package org.stepic.droid.util;

import android.util.Patterns;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

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
        stringBuilder.append("&amv=650");
        stringBuilder.append("&apn=");
        String packageName = MainApplication.getAppContext().getPackageName();
        if (packageName == null){
            return getUriForCourse(config.getBaseUrl(), slug);
        }
        stringBuilder.append(packageName);

//        stringBuilder.append("&ibi=com.AlexKarpov.Stepic");
        return stringBuilder.toString();
    }

//    private static final Pattern urlPattern = Pattern.compile(
//            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
//                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
//                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
//            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);


    //Pull all links from the body for easy retrieval
    public static List<String> pullLinks(String textHtml) {
        String text = HtmlHelper.fromHtml(textHtml).toString();
        List<String> links = new ArrayList<>();

        Matcher m = Patterns.WEB_URL.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            urlStr = urlStr.trim();
            links.add(urlStr);
        }
        return links;
    }

}
