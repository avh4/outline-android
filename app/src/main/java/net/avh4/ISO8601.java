package net.avh4;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601 {

    private static final TimeZone tz = TimeZone.getTimeZone("UTC");
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    static {
        df.setTimeZone(tz);
    }

    public static String format(Date date) {
        return df.format(date);
    }
}
