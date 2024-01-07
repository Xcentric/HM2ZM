package model.homemoney;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import model.commons.CsvRecord;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;

public final class HomeMoneyCsvRecord extends CsvRecord {

    @CsvBindByName(required = true)
    private String account;
    @CsvBindByName
    private String category;
    @CsvBindByName(required = true)
    private Currency currency;
    @CsvBindByName(required = true)
    @CsvDate("dd.MM.yyyy")
    private Date date;
    @CsvBindByName
    private String description;
    @CsvBindByName(required = true, locale = "ru-RU")
    private BigDecimal total;
    @CsvBindByName
    private String transfer;

    public HomeMoneyCsvRecord() {
    }

    /* INTERFACE */

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof HomeMoneyCsvRecord))
            return false;

        HomeMoneyCsvRecord that = (HomeMoneyCsvRecord) obj;

        //@formatter:off
        return     Objects.equals(account, that.account)
                && Objects.equals(category, that.category)
                && Objects.equals(currency, that.currency)
                && Objects.equals(date, that.date)
                && Objects.equals(description, that.description)
                && Objects.equals(total, that.total)
                && Objects.equals(transfer, that.transfer);
        //@formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, category, currency, date, description, total, transfer);
    }

    /* PROPERTIES */

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    @Override
    public boolean isTransfer() {
        return transfer != null;
    }

    @Override
    public boolean isValid() {
        return account != null && currency != null && date != null && total != null;
    }

    /* IMPLEMENTATION */

    @Override
    protected String toString(ToStringStyle style) {
        //@formatter:off
        return new ToStringBuilder(this, style)
                .append("date", formatDate(date))
                .append("account", account)
                .append("category", category)
                .append("total", total)
                .append("currency", currency)
                .append("description", description)
                .append("transfer", transfer)
                .toString();
        //@formatter:on
    }

}
