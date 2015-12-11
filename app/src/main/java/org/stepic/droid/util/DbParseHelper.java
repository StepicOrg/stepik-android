package org.stepic.droid.util;

public class DbParseHelper {

    private static final String DELIMITER = "__,__";

    public static long[] parseStringToLongArray(String str) {
        if (str == null) return null;

        String[] strArray = str.split(DELIMITER);
        long[] result = new long[strArray.length];
        for (int i = 0; i < strArray.length; i++)
            result[i] = Long.parseLong(strArray[i].trim());
        return result;
    }

    public static String parseLongArrayToString(long[] array) {
        return parseLongArrayToString(array, DELIMITER);
    }

    public static String parseLongArrayToString(long[] array, String delimiter) {
        if (array == null || array.length == 0) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append(array[i]);
            if (i != array.length - 1)
                stringBuilder.append(delimiter);
        }
        return stringBuilder.toString();
    }


    public static String parseStringArrayToString(String[] array) {
        if (array == null || array.length == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length - 1)
                sb.append(DELIMITER);
        }
        return sb.toString();
    }

    public static String[] parseStringToStringArray(String str) {
        if (str == null) return null;

        String[] strArray = str.split(DELIMITER);
        String[] result = new String[strArray.length];
        for (int i = 0; i < strArray.length; i++)
            result[i] = strArray[i].trim();
        return result;
    }

    public static String[] parseLongArrayToStringArray(long[] array) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] + "";
        }
        return result;
    }

    public static String parseLongArrayToString(Long[] array, String delimiter) {

        if (array == null || array.length == 0) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append(array[i]);
            if (i != array.length - 1)
                stringBuilder.append(delimiter);
        }
        return stringBuilder.toString();
    }


}
