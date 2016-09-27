package org.stepic.droid.util;

import android.text.Html;
import android.text.Spanned;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.notifications.model.Notification;

public class HtmlHelper {

    private static Spanned fromHtmlLegacy(@Nullable String content) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(content);
        }
        return result;
    }

    @NotNull
    public static CharSequence fromHtml(@Nullable String content) {
        if (content == null)
            return fromHtmlLegacy("");
        String newContent = content.trim().replace("\n", "<br>");

        CharSequence htmlHandled = fromHtmlLegacy(newContent);
        return trimTrailingWhitespace(htmlHandled);
    }

    @NotNull
    public static String getHtmlWhiteSpaces(String content) {
        if (content == null) return "";
        String newContent = content.replace("\n", "<br>");
        return newContent;
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
        //FIXME ADD wysiwyg, REMOVE <img>???
        boolean isContainsWysiwyg = text.contains("wysiwyg-");
        boolean isContainsPicture = text.contains("<img");
        boolean isContainsLatex = text.contains("$");
        boolean isContainsCode = text.contains("<pre><code>");
        boolean isContainsBigMath = text.contains("\\[");
        return isContainsLatex || isContainsPicture || isContainsCode || isContainsBigMath || isContainsWysiwyg;
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
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
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


    public static String buildMathPage(CharSequence body, int widthPx, String baseUrl) {
        String preBody = String.format(PRE_BODY, MathJaxScript, widthPx, baseUrl);
        String result = preBody + body + POST_BODY;
        return result;
    }

    public static String buildPageWithAdjustingTextAndImage(CharSequence body, int widthPx, String baseUrl) {
        String preBody = String.format(PRE_BODY, " ", widthPx, baseUrl);
        String result = preBody + body + POST_BODY;
        return result;
    }

    //string with 2 format args
    private static final String PRE_BODY = "<html>\n" +
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

            + "</style>\n" +

            "<meta name=\"viewport\" content=\"width=" +

            "%d" +

            ", user-scalable=no" +
            ", target-densitydpi=medium-dpi" +
            "\" />" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"wysiwyg.css\"/>" +
            "<base href=\"%s\"" + //FIXME set from configs
            "</head>\n"
            + "<body style='margin:0;padding:0;'>";

    private static final String POST_BODY = "</body>\n" +
            "</html>";

    private static final String MathJaxScript =
            "<script type=\"text/x-mathjax-config\">\n" +
                    "  MathJax.Hub.Config({" +
                    "messageStyle: \"none\", " +
                    "tex2jax: {preview: \"none\", inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
                    "displayMath: [ ['$$','$$'], ['\\[','\\]'] ]" +
                    "</script>\n" +
                    "<script type=\"text/javascript\"\n" +
                    " src=\"file:///android_asset/MathJax/MathJax.js?config=TeX-AMS-MML_HTMLorMML-full\">\n" +
                    "</script>\n";

    public static String getUserPath(IConfig config, int userId) {
        return new StringBuilder()
                .append(config.getBaseUrl())
                .append(AppConstants.WEB_URI_SEPARATOR)
                .append("users")
                .append(AppConstants.WEB_URI_SEPARATOR)
                .append(userId)
                .append("/?from_mobile_app=true")
                .toString();
    }

}
