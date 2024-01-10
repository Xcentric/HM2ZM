package converter;

import model.homemoney.HomeMoneyCsvRecord;
import model.zenmoney.ZenMoneyCsvRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Converter {

    private final Map<String, Set<Currency>> convertedAccounts = new HashMap<>();
    private final Set<String> multiCurrencyAccounts;

    public Converter(Set<String> multiCurrencyAccounts) {
        this.multiCurrencyAccounts = Set.copyOf(multiCurrencyAccounts);
    }

    /* INTERFACE */

    public ZenMoneyCsvRecord convertRecord(HomeMoneyCsvRecord record) {
        if (record == null || record.isTransfer()) {
            throw new IllegalArgumentException("record == " + record);
        }

        ZenMoneyCsvRecord converted = new ZenMoneyCsvRecord();

        converted.setCategoryName(record.getCategory());
        converted.setComment(record.getDescription());
        converted.setDate(record.getDate());

        if (record.getTotal().signum() >= 0) {
            converted.setIncome(record.getTotal());
            converted.setIncomeAccountName(convertAccount(record));
            converted.setIncomeCurrencyShortTitle(record.getCurrency());
        } else {
            converted.setOutcome(record.getTotal().negate());
            converted.setOutcomeAccountName(convertAccount(record));
            converted.setOutcomeCurrencyShortTitle(record.getCurrency());
        }

        return converted;
    }

    public ZenMoneyCsvRecord convertRecord(HomeMoneyCsvRecord transferRecord1, HomeMoneyCsvRecord transferRecord2) {
        if (transferRecord1 == null || !transferRecord1.isTransfer() || transferRecord1.getTotal().signum() != -1) {
            throw new IllegalArgumentException("transferRecord1 == " + transferRecord1);
        }
        if (transferRecord2 == null || !transferRecord2.isTransfer() || transferRecord2.getTotal().signum() != +1) {
            throw new IllegalArgumentException("transferRecord2 == " + transferRecord2);
        }
        if (!transferRecord2.getAccount().equals(transferRecord1.getTransfer())) {
            throw new IllegalArgumentException(
                    "transferRecord1 == " + transferRecord1 + "; transferRecord2 == " + transferRecord2);
        }

        ZenMoneyCsvRecord converted = new ZenMoneyCsvRecord();

        converted.setCategoryName(transferRecord1.getCategory());
        converted.setComment(transferRecord1.getDescription());
        converted.setDate(transferRecord1.getDate());

        converted.setIncome(transferRecord2.getTotal());
        converted.setIncomeAccountName(convertAccount(transferRecord2));
        converted.setIncomeCurrencyShortTitle(transferRecord2.getCurrency());

        converted.setOutcome(transferRecord1.getTotal().negate());
        converted.setOutcomeAccountName(convertAccount(transferRecord1));
        converted.setOutcomeCurrencyShortTitle(transferRecord1.getCurrency());

        return converted;
    }

    public Pair<ZenMoneyCsvRecord, ZenMoneyCsvRecord> splitTransfer(ZenMoneyCsvRecord transferRecord,
            String commonCategoryName) {
        if (transferRecord == null || !transferRecord.isTransfer()) {
            throw new IllegalArgumentException("transferRecord == " + transferRecord);
        }

        ZenMoneyCsvRecord record1 = new ZenMoneyCsvRecord(transferRecord);
        ZenMoneyCsvRecord record2 = new ZenMoneyCsvRecord(transferRecord);

        record1.setCategoryName(commonCategoryName);
        record2.setCategoryName(commonCategoryName);

        record1.setIncome(null);
        record1.setIncomeAccountName(null);
        record1.setIncomeCurrencyShortTitle(null);

        record2.setOutcome(null);
        record2.setOutcomeAccountName(null);
        record2.setOutcomeCurrencyShortTitle(null);

        return Pair.of(record1, record2);
    }

    /* PROPERTIES */

    public Map<String, Set<Currency>> getConvertedAccounts() {
        Map<String, Set<Currency>> result = new HashMap<>(convertedAccounts.size());

        convertedAccounts.forEach((account, currencies) -> result.put(account, Set.copyOf(currencies)));

        return Collections.unmodifiableMap(result);
    }

    /* IMPLEMENTATION */

    private String convertAccount(HomeMoneyCsvRecord record) {
        String account = record.getAccount();

        if (multiCurrencyAccounts.contains(account)) {
            account += " (" + record.getCurrency().getCurrencyCode() + ')';
        }
        convertedAccounts.computeIfAbsent(account, (key) -> new HashSet<>()).add(record.getCurrency());

        return account;
    }

}
