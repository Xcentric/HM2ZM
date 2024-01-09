package application;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.processor.RowProcessor;
import converter.Converter;
import model.homemoney.HomeMoneyCsvRecord;
import model.zenmoney.ZenMoneyCsvRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "HM2ZM", version = "HM2ZM v1.0", description = "Convert HomeMoney CSV to ZenMoney CSV.",
         mixinStandardHelpOptions = true)
public final class Application implements Callable<Integer> {

    public static final class ExitCodes {

        public static final int UNHANDLED_EXCEPTION = -1;
        public static final int OK = 0;
        public static final int UNRECOVERABLE_EXCEPTION = 1;    // handled by picocli
        public static final int INVALID_OPTIONS = 2;            // handled by picocli
        public static final int CONVERSION_COMPLETED_WITH_ERRORS = 3;

        private ExitCodes() {
        }

    }

    private static class BlankColumnsToNullProcessor implements RowProcessor {

        @Override
        public String processColumnItem(String column) {
            if (StringUtils.isBlank(column)) {
                return null;
            }

            return column;
        }

        @Override
        public void processRow(String[] row) {
            for (int i = 0; i < row.length; i++) {
                row[i] = processColumnItem(row[i]);
            }
        }

    }

    private static final RowProcessor ROW_PROCESSOR = new BlankColumnsToNullProcessor();

    @Spec
    private CommandSpec commandSpec;

    @Option(names = "--input-file", paramLabel = "<path>", required = true,
            description = "Path to HomeMoney CSV file being converted.")
    private Path inputFile;

    @Option(names = "--multi-currency-account", paramLabel = "<account>",
            description =
                    "Multi-valued (i.e. may be included several times) option for specifying multi-currency " +
                            "accounts. Case-sensitive.")
    private Set<String> multiCurrencyAccounts = Set.of();

    @Option(names = "--output-file", paramLabel = "<path>", required = true,
            description = "Path to converted ZenMoney CSV file.")
    private Path outputFile;

    private int splitOutputBy;

    /* INTERFACE */

    @Override
    public Integer call() throws Exception {
        printLine("Converting file: " + inputFile.toString());

        int errorCount;
        try (Reader inputFileReader = newFileReader(inputFile)) {
            errorCount = convert(newCsvBeaner(inputFileReader));
        }

        if (errorCount == 0) {
            printLine("Conversion completed with no errors.");

            return ExitCodes.OK;
        } else {
            printLine("Conversion completed with errors. Error count: " + errorCount);

            return ExitCodes.CONVERSION_COMPLETED_WITH_ERRORS;
        }
    }

    public static int run(String[] args) {
        return new CommandLine(new Application()).execute(args);
    }

    /* PROPERTIES */

    @Option(names = "--split-output-by", paramLabel = "<N>", defaultValue = "0",
            description = {"Split output file into multiple files with <N> lines each.",
                    "If omitted or set as '0' then the output file won't be split."})
    protected void setSplitOutputBy(int splitOutputBy) {
        if (splitOutputBy < 0) {
            throw new ParameterException(commandSpec.commandLine(),
                    String.format("Invalid value '%d' for option '--split-output-by': value is not a natural number.",
                            splitOutputBy));
        }

        this.splitOutputBy = splitOutputBy;
    }

    /* IMPLEMENTATION */

    private int convert(CsvToBean<HomeMoneyCsvRecord> csvBeaner) throws IOException {
        Converter converter = new Converter(multiCurrencyAccounts);
        Iterator<HomeMoneyCsvRecord> records = csvBeaner.iterator();
        Writer outputFileWriter = null;
        StatefulBeanToCsv<ZenMoneyCsvRecord> beanToCsv = null;
        int recordCount = 0, errorCount = 0, splitOutputCounter = 0, outputFileOrderNumber = 0;
        HomeMoneyCsvRecord prevTransferRecord = null;

        try {
            while (records.hasNext()) {
                try {
                    if (outputFileWriter == null) {
                        outputFileWriter = newFileWriter(outputFile, recordCount == 0 ? null : ++outputFileOrderNumber);
                        beanToCsv = new StatefulBeanToCsvBuilder<ZenMoneyCsvRecord>(outputFileWriter).build();
                    }

                    recordCount++;
                    HomeMoneyCsvRecord record = records.next();

                    printLine(String.format("Converting record (%06d): %s", recordCount, record.toDisplayString()));

                    if (!record.isValid()) {
                        printError("Record is not valid, skipping.");

                        prevTransferRecord = null;  // precaution
                        errorCount++;
                        continue;
                    }

                    /* <CONVERTING> */

                    ZenMoneyCsvRecord converted;
                    if (!record.isTransfer()) {
                        converted = converter.convertRecord(record);
                    } else {
                        if (prevTransferRecord == null) {
                            printLine("Transfer detected, proceeding to the next record.");

                            prevTransferRecord = record;
                            continue;
                        } else {
                            converted = converter.convertRecord(prevTransferRecord, record);
                            prevTransferRecord = null;
                        }
                    }

                    /* </CONVERTING> */

                    printLine(String.format("Converted  record (%06d): %s", recordCount, converted.toDisplayString()));

                    if (!converted.isValid()) {
                        printError("Converted record is not valid, skipping.");

                        errorCount++;
                        continue;
                    }

                    Objects.requireNonNull(beanToCsv, "beanToCsv").write(converted);
                    splitOutputCounter++;

                    if (splitOutputBy > 0 && splitOutputCounter >= splitOutputBy) {
                        printLine("Current output file limit reached, closing.");

                        splitOutputCounter = 0;
                        outputFileWriter.close();
                        outputFileWriter = null;
                        beanToCsv = null;
                    }
                } catch (Exception e) {
                    printError("Exception while converting record " + recordCount + '.');

                    prevTransferRecord = null;  // precaution
                    e.printStackTrace();
                    errorCount++;
                }
            }
        } finally {
            if (outputFileWriter != null) {
                outputFileWriter.close();
            }
        }

        printLine("List of suggested accounts to create at ZenMoney:");
        converter.getConvertedAccounts().stream().sorted().forEachOrdered(Application::printLine);

        if (!csvBeaner.getCapturedExceptions().isEmpty()) {
            printError("List of exceptions that occurred during parsing of the input file:");

            for (CsvException e : csvBeaner.getCapturedExceptions()) {
                printError("Line " + e.getLineNumber() + ": " + e.getMessage() + " | Parsed data: " + ArrayUtils
                        .toString(e.getLine()));
            }

            errorCount += csvBeaner.getCapturedExceptions().size();
        }

        return errorCount;
    }

    private CsvToBean<HomeMoneyCsvRecord> newCsvBeaner(Reader reader) {
        CSVParser csvParser = newCsvParser();
        CSVReader csvReader = newCsvReader(reader, csvParser);

        //@formatter:off
        return new CsvToBeanBuilder<HomeMoneyCsvRecord>(csvReader)
                .withType(HomeMoneyCsvRecord.class)
                .withThrowExceptions(false)
                .build();
        //@formatter:on
    }

    private CSVParser newCsvParser() {
        //@formatter:off
        return new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreQuotations(true)
                .withEscapeChar(ICSVParser.NULL_CHARACTER)
                .build();
        //@formatter:on
    }

    private CSVReader newCsvReader(Reader reader, CSVParser csvParser) {
        //@formatter:off
        return new CSVReaderBuilder(reader)
                .withCSVParser(csvParser)
                .withRowProcessor(ROW_PROCESSOR)
                .build();
        //@formatter:on
    }

    private static Reader newFileReader(Path path) throws IOException {
        InputStream origin = Files.newInputStream(path);
        BOMInputStream inputStream = BOMInputStream.builder().setInputStream(origin).get();
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8.newDecoder());

        return new BufferedReader(reader);
    }

    private static Writer newFileWriter(Path path, Integer suffixNumber) throws IOException {
        String fileName = path.toString();

        if (suffixNumber != null) {
            String extension = FilenameUtils.getExtension(fileName);
            fileName = FilenameUtils.removeExtension(fileName) + suffixNumber;
            if (StringUtils.isNotEmpty(extension)) {
                fileName += FilenameUtils.EXTENSION_SEPARATOR + extension;
            }
        }

        return new FileWriter(fileName, StandardCharsets.UTF_8);
    }

    private static void printError(String error) {
        System.err.println(error);
    }

    private static void printLine(String line) {
        System.out.println(line);
    }

}
