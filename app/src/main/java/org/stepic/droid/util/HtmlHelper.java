package org.stepic.droid.util;

import android.text.Html;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.stepic.droid.notifications.model.Notification;

public class HtmlHelper {

    @NotNull
    public static CharSequence fromHtml(@Nullable String content) {
        if (content == null)
            return Html.fromHtml("");

        return Html.fromHtml(content);
    }

    /**
     * Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     *
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

    public static boolean isForWebView(@NotNull String text) {
        boolean isContainsPicture = text.contains("<img");
        boolean isContainsLatex = text.contains("$");
        return isContainsLatex || isContainsPicture;
    }

    /**
     * get meta value
     *
     * @param htmlText with meta tags
     * @param metaKey  meta key of 'name' attribute in meta tag
     * @return value of 'content' attribute in tag meta with 'name' == metaKey
     */
    @Nullable
    public static String getValueOfMetaOrNull(String htmlText, String metaKey) {
        Document document = Jsoup.parse(htmlText);
        Elements elements = document.select("meta");
        try {
            return elements.attr("name", metaKey).last().attr("content"); //WTF? first is csrf param, but jsoup can't handle
        } catch (Exception ex) {
            return "";
        }
    }

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
            String number = slug.substring(indexOfLastDash + 1, slug.length());
            id = Long.parseLong(number);
        } catch (NumberFormatException | IndexOutOfBoundsException notCorrectSlug) {
            YandexMetrica.reportError(AppConstants.ERROR_PARSING_SLUG, notCorrectSlug);
        }
        return id;
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


    public static String buildMathPage(CharSequence body, int widthPx) {
        String preBody = String.format(PRE_BODY, MathJaxScript, widthPx);
        String result = preBody + body + POST_BODY;
        return result;
    }

    public static String buildPageWithAdjustingTextAndImage(CharSequence body, int widthPx) {
        String preBody = String.format(PRE_BODY, " ", widthPx);
        String result = preBody + body + POST_BODY;
        return result;
    }

    //string with 2 format args
    public static final String PRE_BODY = "<html>\n" +
            "<head>\n" +
            "<title>Step</title>\n" +

            "%s" +

            "<style>\n"
            + "\nhtml{-webkit-text-size-adjust: 100%%;}"
            + "\nbody{font-size: 12pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;}"
            + "\nh1{font-size: 20pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;}"
            + "\nh2{font-size: 17pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;}"
            + "\nh3{font-size: 14pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;}"
            + "\nimg { max-width: 100%%; }"
            + "\np{margin: 0px; padding: 0px; display: inline;}"
            + "<meta name=\"viewport\" content=\"width=" +

            "%d" +

            ", user-scalable=no\" />" +
            "</style>\n" +
            "</head>\n"
            + "<body style='margin:0;padding:0;'>";

    public static final String POST_BODY = "</body>\n" +
            "</html>";

    public static final String MathJaxScript =
            "<script type=\"text/x-mathjax-config\">\n" +
                    "  MathJax.Hub.Config({" +
                    "messageStyle: \"none\", " +
                    "tex2jax: {preview: \"none\", inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
                    "</script>\n" +
                    "<script type=\"text/javascript\"\n" +
                    " src=\"file:///android_asset/MathJax/MathJax.js?config=TeX-AMS-MML_HTMLorMML-full\">\n" +
                    "</script>\n";
}
