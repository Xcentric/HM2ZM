package model.zenmoney;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import model.commons.CsvRecord;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;

public final class ZenMoneyCsvRecord extends CsvRecord {

    @CsvBindByName
    private String categoryName;
    @CsvBindByName
    private String comment;
    @CsvBindByName(required = true)
    @CsvDate("yyyy-MM-dd")
    private Date date;
    @CsvBindByName
    private BigDecimal income;
    @CsvBindByName
    private String incomeAccountName;
    @CsvBindByName
    private Currency incomeCurrencyShortTitle;
    @CsvBindByName
    private BigDecimal outcome;
    @CsvBindByName
    private String outcomeAccountName;
    @CsvBindByName
    private Currency outcomeCurrencyShortTitle;

    public ZenMoneyCsvRecord() {
    }

    public ZenMoneyCsvRecord(ZenMoneyCsvRecord other) {
        this.categoryName = other.categoryName;
        this.comment = other.comment;
        this.date = other.date;
        this.income = other.income;
        this.incomeAccountName = other.incomeAccountName;
        this.incomeCurrencyShortTitle = other.incomeCurrencyShortTitle;
        this.outcome = other.outcome;
        this.outcomeAccountName = other.outcomeAccountName;
        this.outcomeCurrencyShortTitle = other.outcomeCurrencyShortTitle;
    }

    /* INTERFACE */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZenMoneyCsvRecord)) {
            return false;
        }

        ZenMoneyCsvRecord that = (ZenMoneyCsvRecord) obj;

        // @formatter:off
        return     Objects.equals(categoryName, that.categoryName)
                && Objects.equals(comment, that.comment)
                && Objects.equals(date, that.date)
                && Objects.equals(income, that.income)
                && Objects.equals(incomeAccountName, that.incomeAccountName)
                && Objects.equals(incomeCurrencyShortTitle, that.incomeCurrencyShortTitle)
                && Objects.equals(outcome, that.outcome)
                && Objects.equals(outcomeAccountName, that.outcomeAccountName)
                && Objects.equals(outcomeCurrencyShortTitle, that.outcomeCurrencyShortTitle);
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, comment, date, income, incomeAccountName, incomeCurrencyShortTitle, outcome,
                outcomeAccountName, outcomeCurrencyShortTitle);
    }

    /* PROPERTIES */

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        if (income != null && income.signum() == -1) {
            throw new IllegalArgumentException("income == " + income);
        }

        this.income = income;
    }

    public String getIncomeAccountName() {
        return incomeAccountName;
    }

    public void setIncomeAccountName(String incomeAccountName) {
        this.incomeAccountName = incomeAccountName;
    }

    public Currency getIncomeCurrencyShortTitle() {
        return incomeCurrencyShortTitle;
    }

    public void setIncomeCurrencyShortTitle(Currency incomeCurrencyShortTitle) {
        this.incomeCurrencyShortTitle = incomeCurrencyShortTitle;
    }

    public BigDecimal getOutcome() {
        return outcome;
    }

    public void setOutcome(BigDecimal outcome) {
        if (outcome != null && outcome.signum() == -1) {
            throw new IllegalArgumentException("outcome == " + outcome);
        }

        this.outcome = outcome;
    }

    public String getOutcomeAccountName() {
        return outcomeAccountName;
    }

    public void setOutcomeAccountName(String outcomeAccountName) {
        this.outcomeAccountName = outcomeAccountName;
    }

    public Currency getOutcomeCurrencyShortTitle() {
        return outcomeCurrencyShortTitle;
    }

    public void setOutcomeCurrencyShortTitle(Currency outcomeCurrencyShortTitle) {
        this.outcomeCurrencyShortTitle = outcomeCurrencyShortTitle;
    }

    @Override
    public boolean isTransfer() {
        return outcomeAccountName != null && incomeAccountName != null;
    }

    @Override
    public boolean isValid() {
        //@formatter:off
        return date != null
                && (    (income != null && incomeAccountName != null && incomeCurrencyShortTitle != null)
                     || (outcome != null && outcomeAccountName != null && outcomeCurrencyShortTitle != null))
                && !Objects.equals(incomeAccountName, outcomeAccountName);
        //@formatter:on
    }

    /* IMPLEMENTATION */

    @Override
    protected String toString(ToStringStyle style) {
        //@formatter:off
        return new ToStringBuilder(this, style)
                .append("date", formatDate(date))
                .append("categoryName", categoryName)
                .append("comment", comment)
                .append("outcomeAccountName", outcomeAccountName)
                .append("outcome", outcome)
                .append("outcomeCurrencyShortTitle", outcomeCurrencyShortTitle)
                .append("incomeAccountName", incomeAccountName)
                .append("income", income)
                .append("incomeCurrencyShortTitle", incomeCurrencyShortTitle)
                .toString();
        //@formatter:on
    }

}
