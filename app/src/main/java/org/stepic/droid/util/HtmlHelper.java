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
}
