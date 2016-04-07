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

    public static Long parseCourseIdFromNotification(String htmlRaw) {
        StringBuilder raw = new StringBuilder();
        boolean flag = false;
        for (int i = 0; i < htmlRaw.length(); i++) {
            if (htmlRaw.charAt(i) == '<') {
                flag = true;
                continue;
            }

            if (htmlRaw.charAt(i) == '>')
                break;

            if (flag)
                raw.append(htmlRaw.charAt(i));
        }

        if (raw.length() > 0) {
            String[] splitted = raw.toString().split("-");
            if (splitted.length > 0) {
                String numb = splitted[splitted.length - 1];
                StringBuilder n = new StringBuilder();
                for (int i = 0; i < numb.length(); i++) {
                    if (Character.isDigit(numb.charAt(i)) == true) {
                        n.append(numb.charAt(i));
                    }
                }

                if (n.length() > 0)
                    return Long.parseLong(n.toString());
                return null;
            }
        }

        return null;
    }
}
