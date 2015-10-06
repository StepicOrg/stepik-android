package org.stepic.droid.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

public final class DateTimeHelper {
    public static String getPresentOfDate(String dateInISOFormat, DateTimeFormatter mFormatForView, DateTime dateTime) {
        if (dateInISOFormat == null) return "";
        dateTime = new DateTime(dateInISOFormat);
        return mFormatForView.print(dateTime);
    }
}
