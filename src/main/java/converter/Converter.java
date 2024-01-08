package converter;

import model.homemoney.HomeMoneyCsvRecord;
import model.zenmoney.ZenMoneyCsvRecord;

public final class Converter {

    private Converter() {
    }

    /* INTERFACE */

    public static ZenMoneyCsvRecord convertRecord(HomeMoneyCsvRecord record) {
        if (record == null || record.isTransfer()) {
            throw new IllegalArgumentException("record == " + record);
        }

        ZenMoneyCsvRecord converted = new ZenMoneyCsvRecord();

        converted.setCategoryName(record.getCategory());
        converted.setComment(record.getDescription());
        converted.setDate(record.getDate());

        if (record.getTotal().signum() >= 0) {
            converted.setIncome(record.getTotal());
            converted.setIncomeAccountName(record.getAccount());
            converted.setIncomeCurrencyShortTitle(record.getCurrency());
        } else {
            converted.setOutcome(record.getTotal().negate());
            converted.setOutcomeAccountName(record.getAccount());
            converted.setOutcomeCurrencyShortTitle(record.getCurrency());
        }

        return converted;
    }

    public static ZenMoneyCsvRecord convertRecord(HomeMoneyCsvRecord transferRecord1,
                                                  HomeMoneyCsvRecord transferRecord2) {
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
        converted.setIncomeAccountName(transferRecord2.getAccount());
        converted.setIncomeCurrencyShortTitle(transferRecord2.getCurrency());

        converted.setOutcome(transferRecord1.getTotal().negate());
        converted.setOutcomeAccountName(transferRecord1.getAccount());
        converted.setOutcomeCurrencyShortTitle(transferRecord1.getCurrency());

        return converted;
    }

}
