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
import com.opencsv.processor.RowProcessor;
import converter.Converter;
import model.homemoney.HomeMoneyCsvRecord;
import model.zenmoney.ZenMoneyCsvRecord;
import org.apache.commons.io.input.BOMInputStream;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "HM2ZM", version = "HM2ZM v1.0", description = "Convert HomeMoney CSV to ZenMoney CSV.",
         mixinStandardHelpOptions = true)
public final class Application implements Callable<Integer> {

    public static final class ExitCodes {

        public static final int UNHANDLED_EXCEPTION = -1;
        public static final int OK = 0;
        public static final int UNRECOVERABLE_EXCEPTION = 1;    // provided by picocli
        public static final int INVALID_OPTIONS = 2;            // provided by picocli
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
                            "accounts.")
    private Set<String> multiCurrencyAccount;

    @Option(names = "--output-file", paramLabel = "<path>", required = true,
            description = "Path to converted ZenMoney CSV file.")
    private Path outputFile;

    private int splitOutputBy;

    /* INTERFACE */

    @Override
    public Integer call() throws Exception {
        println("Converting file: " + inputFile.toString());

        try (Reader inputFileReader = newFileReader(inputFile)) {
            CsvToBean<HomeMoneyCsvRecord> csvBeaner = newCsvBeaner(inputFileReader);
            List<HomeMoneyCsvRecord> csvRecords = csvBeaner.parse();

            // TODO: implement with: splitOutputBy, multiCurrencyAccount
            try (Writer outputFileWriter = new FileWriter(outputFile.toString())) {
                StatefulBeanToCsv<ZenMoneyCsvRecord> beanToCsv =
                        new StatefulBeanToCsvBuilder<ZenMoneyCsvRecord>(outputFileWriter).build();

                HomeMoneyCsvRecord prevTransferRecord = null;
                for (HomeMoneyCsvRecord record : csvRecords) {
                    println("Processing record: " + record.toDisplayString());

                    ZenMoneyCsvRecord converted;

                    if (!record.isTransfer()) {
                        converted = Converter.convertRecord(record);
                    } else {
                        if (prevTransferRecord == null) {
                            prevTransferRecord = record;
                            continue;
                        } else {
                            converted = Converter.convertRecord(prevTransferRecord, record);
                            prevTransferRecord = null;
                        }
                    }

                    println("Converted record: " + converted.toDisplayString());

                    beanToCsv.write(converted);

                    println("Converted record was written.");
                }
            }
        }

        println("Conversion complete.");
        return ExitCodes.OK;
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

    private CsvToBean<HomeMoneyCsvRecord> newCsvBeaner(Reader reader) {
        CSVParser csvParser = newCsvParser();
        CSVReader csvReader = newCsvReader(reader, csvParser);

        return new CsvToBeanBuilder<HomeMoneyCsvRecord>(csvReader).withType(HomeMoneyCsvRecord.class).build();
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

    private static void println(String line) {
        System.out.println(line);
    }

}
