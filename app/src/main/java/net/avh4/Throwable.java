package net.avh4;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Throwable {
    public static String getStackTraceAsString(java.lang.Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }
}
