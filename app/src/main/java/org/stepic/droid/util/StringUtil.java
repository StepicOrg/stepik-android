package org.stepic.droid.util;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

public class StringUtil {
    public static Double safetyParseString(String str) {
        Double doubleScore = null;
        try {
            doubleScore = Double.parseDouble(str);

        } catch (Exception ignored) {
        }
        return doubleScore;
    }

    public static String getUriForCourse(String baseUrl, String slug){
        StringBuilder stringBuilder =new StringBuilder();
        stringBuilder.append(baseUrl);
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);
        stringBuilder.append("course");
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);
        stringBuilder.append(slug);
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);
        return stringBuilder.toString();
    }

    public static String getDynamicLinkForCourse(IConfig config, String slug){
        String firebaseDomain = config.getFirebaseDomain();
        if (firebaseDomain == null){
            return getUriForCourse(config.getBaseUrl(), slug);
        }


        StringBuilder stringBuilder =new StringBuilder();
        stringBuilder.append(firebaseDomain);
        stringBuilder.append("?link=");
        stringBuilder.append(config.getBaseUrl());
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);
        stringBuilder.append("course");
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);
        stringBuilder.append(slug);
        stringBuilder.append(AppConstants.WEB_URI_SEPARATOR);

        stringBuilder.append("&apn=");
        String packageName = MainApplication.getAppContext().getPackageName();
        if (packageName == null){
            return getUriForCourse(config.getBaseUrl(), slug);
        }
        stringBuilder.append(packageName);

//        stringBuilder.append("&ibi=com.AlexKarpov.Stepic");

        stringBuilder.append("&amv=650");
        return stringBuilder.toString();
    }

}
