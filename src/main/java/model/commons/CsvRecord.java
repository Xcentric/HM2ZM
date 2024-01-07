package model.commons;

import org.apache.commons.lang3.builder.ToStringStyle;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class CsvRecord {

    private static class DisplayStringStyle extends ToStringStyle {

        private DisplayStringStyle() {
            setUseClassName(false);
            setUseIdentityHashCode(false);

            this.setContentStart("{");
            this.setContentEnd("}");

            this.setArrayStart("[");
            this.setArrayEnd("]");

            this.setFieldSeparator(", ");
            this.setFieldNameValueSeparator(": ");

            this.setNullText(null);
        }

    }

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final ToStringStyle DISPLAY_STRING_STYLE = new DisplayStringStyle();

    /* INTERFACE */

    public final String toDisplayString() {
        return toString(DISPLAY_STRING_STYLE);
    }

    @Override
    public final String toString() {
        return toString(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /* PROPERTIES */

    public abstract boolean isTransfer();

    public abstract boolean isValid();

    /* IMPLEMENTATION */

    protected String formatDate(Date date) {
        return DISPLAY_DATE_FORMAT.format(date);
    }

    protected abstract String toString(ToStringStyle style);

}
