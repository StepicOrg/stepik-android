package org.stepic.droid.util;

public class DbParseHelper {

    private static final String DELIMETER = "__,__";

    public static long[] parseStringToLongArray(String str) {
        if (str == null) return null;

        String[] strArray = str.split(DELIMETER);
        long[] result = new long[strArray.length];
        for (int i = 0; i < strArray.length; i++)
            result[i] = Long.parseLong(strArray[i].trim());
        return result;
    }

    public static String parseLongArrayToString(long[] array) {
        if (array == null || array.length == 0) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append(array[i]);
            if (i != array.length - 1)
                stringBuilder.append(DELIMETER);
        }
        return stringBuilder.toString();
    }

    public static String parseStringArrayToString(String[] array) {
        if (array == null || array.length == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length - 1)
                sb.append(DELIMETER);
        }
        return sb.toString();
    }

    public static String[] parseStringToStringArray(String str) {
        if (str == null) return null;

        String[] strArray = str.split(DELIMETER);
        String[] result = new String[strArray.length];
        for (int i = 0; i < strArray.length; i++)
            result[i] = strArray[i].trim();
        return result;
    }
}
