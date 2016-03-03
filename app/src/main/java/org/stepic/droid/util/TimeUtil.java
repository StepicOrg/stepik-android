package org.stepic.droid.util;

public class TimeUtil {
    public static String getFormattedVideoTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return getRepresentation(minutes) + " : " + getRepresentation(seconds);
    }

    private static String getRepresentation(long number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
