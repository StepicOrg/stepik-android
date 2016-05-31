package org.stepic.droid.util;

import android.text.Html;
import android.text.Spanned;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.notifications.model.Notification;

import java.util.List;

public class HtmlHelper {

    @NotNull
    public static Spanned fromHtml(@Nullable String content) {
        if (content == null)
            return Html.fromHtml("");

        return Html.fromHtml(content);
    }

    public static boolean isForWebView(@NotNull String text) {
        return text.contains("$") || text.contains("<img");
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
        Source source = new Source(htmlText);
        String strData = "";
        List<Element> elements = source.getAllElements("meta");

        for (Element element : elements) {
            final String id = element.getAttributeValue("name"); // Get Attribute 'id'
            if (id != null && id.equals(metaKey)) {
                strData = element.getAttributeValue("content");
            }
        }
        return strData;
    }

    @Nullable
    public static Long parseCourseIdFromNotification(@NotNull Notification notification) {
        String htmlRaw = notification.getHtmlText();
        if (htmlRaw == null) return null;
        return parseCourseIdFromNotification(htmlRaw);
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
