package org.stepic.droid.util;

import android.text.Html;
import android.text.Spanned;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HtmlHelper {

    public static Spanned fromHtml(@Nullable String content) {
        if (content == null)
            return Html.fromHtml("");

        return Html.fromHtml(content);
    }

    /**
     * get meta value
     * @param htmlText with meta tags
     * @param metaKey meta key of 'name' attribute in meta tag
     * @return value of 'content' attribute in tag meta with 'name' == metaKey
     */
    @Nullable
    public static String getValueOfMetaOrNull(String htmlText, String metaKey){
        Source source = new Source(htmlText);
        String strData = "";
        List<Element> elements = source.getAllElements("meta");

        for(Element element : elements )
        {
            final String id = element.getAttributeValue("name"); // Get Attribute 'id'
            if( id != null && id.equals(metaKey)){
                strData = element.getAttributeValue("content");
            }
        }
        return strData;
    }
}
