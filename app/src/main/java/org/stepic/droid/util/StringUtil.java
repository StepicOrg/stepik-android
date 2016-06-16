package org.stepic.droid.util;

import org.jetbrains.annotations.Nullable;

public class StringUtil {
    public static Double safetyParseString(String str) {
        Double doubleScore = null;
        try {
            doubleScore = Double.parseDouble(str);

        } catch (Exception ignored) {
        }
        return doubleScore;
    }

    @Nullable
    public static String join(String[] values, String delimiter) {
        if (values == null || values.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i != values.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}
