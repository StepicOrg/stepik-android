package org.stepic.droid.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.stepic.droid.notifications.model.Notification;

import timber.log.Timber;

public class HtmlHelper {
    @Nullable
    public static Long parseCourseIdFromNotification(@NotNull Notification notification) {
        String htmlRaw = notification.getHtmlText();
        if (htmlRaw == null) return null;
        return parseCourseIdFromNotification(htmlRaw);
    }

    @Nullable
    public static Long parseIdFromSlug(String slug) {
        Long id = null;
        try {
            id = Long.parseLong(slug);
        } catch (NumberFormatException ignored) {
            //often it is not number then it is "Some-Name-idNum" or just "-idNum"
        }

        if (id != null) {
            //if, for example, -432 -> 432
            return Math.abs(id);
        }

        int indexOfLastDash = slug.lastIndexOf("-");
        if (indexOfLastDash < 0)
            return null;

        try {
            String number = slug.substring(indexOfLastDash + 1);
            id = Long.parseLong(number);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
        }
        return id;
    }

    private final static String syllabusModulePrefix = "syllabus?module=";

    public static Integer parseModulePositionFromNotification(String htmlRaw) {
        int indexOfStart = htmlRaw.indexOf(syllabusModulePrefix);
        if (indexOfStart < 0) return null;

        String begin = htmlRaw.substring(indexOfStart + syllabusModulePrefix.length());
        int end = begin.indexOf("\"");
        String substring = begin.substring(0, end);

        try {
            return Integer.parseInt(substring);
        } catch (Exception exception) {
            return null;
        }
    }

    private static Long parseCourseIdFromNotification(String htmlRaw) {
        int start = htmlRaw.indexOf('<');
        int end = htmlRaw.indexOf('>');
        if (start == -1 || end == -1) return null;
        String substring = htmlRaw.substring(start, end);

        String[] resultOfSplit = substring.split("-");

        if (resultOfSplit.length > 0) {
            String numb = resultOfSplit[resultOfSplit.length - 1];
            StringBuilder n = new StringBuilder();
            for (int i = 0; i < numb.length(); i++) {
                if (Character.isDigit(numb.charAt(i))) {
                    n.append(numb.charAt(i));
                }
            }

            if (n.length() > 0)
                return Long.parseLong(n.toString());
            return null;
        }

        return null;
    }

    @Nullable
    public static String parseNLinkInText(@NotNull String htmlText, String baseUrl, int position) {
        try {
            Document document = Jsoup.parse(htmlText);
            document.setBaseUri(baseUrl);
            Elements elements = document.getElementsByTag("a");
            Element our = elements.get(position);
            String absolute = our.absUrl("href");
            Timber.d(absolute);
            return absolute;
        } catch (Exception exception) {
            return null;
        }
    }
}
