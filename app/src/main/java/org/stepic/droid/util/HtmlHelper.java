package org.stepic.droid.util;

import android.os.Build;
import android.support.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.notifications.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.collections.CollectionsKt;
import timber.log.Timber;

public class HtmlHelper {

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
        //FIXME  REMOVE <img>??? and make ImageGetter with simple textview
        //TODO: REGEXP IS SLOWER
        return text.contains("$")
                || text.contains("wysiwyg-")
                || text.contains("<h")
                || text.contains("\\[")
                || text.contains("<pre><code")
                || text.contains("kotlin-runnable")
                || text.contains("<img")
                || text.contains("<iframe")
                || text.contains("<audio");
    }


    public static boolean hasLaTeX(String textString) {
        return textString.contains("$") || textString.contains("\\[");
    }

    private static boolean hasKotlinRunnableSample(String text) {
        return text.contains("kotlin-runnable");
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


    private static String getStyle(float fontSize, @Nullable String fontPath, @ColorInt int textColorHighlight) {
        final String fontStyle;
        if (fontPath  == null) {
            fontStyle = String.format(Locale.getDefault(), DefaultFontStyle, fontSize);
        } else {
            fontStyle = String.format(Locale.getDefault(), CustomFontStyle, fontPath, fontSize);
        }

        final String selectionColorStyle = String.format(Locale.getDefault(), SelectionColorStyle, 0xFFFFFF & textColorHighlight);
        return fontStyle + selectionColorStyle;
    }

    private static String buildPage(CharSequence body, List<String> additionalScripts, float fontSize, String fontPath, @ColorInt int textColorHighlight, int widthPx, String baseUrl) {
        if (hasKotlinRunnableSample(body.toString())) {
            additionalScripts.add(KotlinRunnableSamplesScript);
        }
        String scripts = CollectionsKt.joinToString(additionalScripts, "", "", "", -1, "", null);
        String preBody = String.format(Locale.getDefault(), PRE_BODY, scripts, getStyle(fontSize, fontPath, textColorHighlight), widthPx, baseUrl);

        return preBody + body + POST_BODY;
    }

    public static String buildMathPage(CharSequence body, @ColorInt int textColorHighlight, int widthPx, String baseUrl) {
        return buildPage(body, CollectionsKt.arrayListOf(MathJaxScript), DefaultFontSize, null, textColorHighlight, widthPx, baseUrl);
    }

    public static String buildPageWithAdjustingTextAndImage(CharSequence body, @ColorInt int textColorHighlight, int widthPx, String baseUrl) {
        return buildPage(body, CollectionsKt.<String>emptyList(), DefaultFontSize, null, textColorHighlight, widthPx, baseUrl);
    }

    public static String buildPageWithCustomFont(CharSequence body, float fontSize, String fontPath, @ColorInt int textColorHighlight, int widthPx, String baseUrl) {
        return buildPage(body, CollectionsKt.<String>emptyList(), fontSize, fontPath, textColorHighlight, widthPx, baseUrl);
    }

    public static final String HORIZONTAL_SCROLL_LISTENER = "scrollListener";
    private static final String HORIZONTAL_SCROLL_STYLE;

    // this block is needed to force render of WebView
    private static final String MIN_RENDERED_BLOCK =
            "<div style=\"height: 1px; overflow: hidden; width: 1px; background-color: rgba(0,0,0,0.001); pointer-events: none; user-select: none; -webkit-user-select: none;\"></div>";

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            HORIZONTAL_SCROLL_STYLE =
                    "<style>\n" +
                    "body > * {\n" +
                    "    max-width: 100%%;\n" +
                    "    overflow-x: scroll;\n" +
                    "    vertical-align: middle;\n" +
                    "}\n" +
                    "body > .no-scroll {\n" +
                    "    overflow: visible !important;\n" +
                    "}" +
                    "</style>\n";
        } else {
            HORIZONTAL_SCROLL_STYLE = "";
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            POST_BODY =
                    MIN_RENDERED_BLOCK +
                    "</body>\n" +
                    "</html>";
        } else {
            POST_BODY =
                    "</body>\n" +
                    "</html>";
        }
    }

    private static final String KotlinRunnableSamplesScript =
            "<script src=\"https://unpkg.com/kotlin-playground@1\"></script>" +
            "<script>\n" +
            "document.addEventListener('DOMContentLoaded', function() {\n" +
            "    KotlinPlayground('kotlin-runnable', { \n" +
            "        callback: function(targetNode, mountNode) {\n" +
            "            mountNode.classList.add('no-scroll');\n" + // disable overflow for pg divs in order to disable overflow and show expand button
            "        }\n" +
            "    });\n" +
            "});\n" +
            "</script>";

    private static final String KOTLIN_PLAYGROUND_SCROLL_RULE =
            "elem.className !== 'CodeMirror-scroll' && elem.className !== 'code-output'";

    private static final String DEFAULT_SCROLL_RULE =
            "elem.parentElement.tagName !== 'BODY' && elem.parentElement.tagName !== 'HTML'";

    private static final String HORIZONTAL_SCROLL_SCRIPT =
            "<script type=\"text/javascript\">\n" +
                "function measureScroll(x, y) {" +
                    "var elem = document.elementFromPoint(x, y);" +
                    "while(" + DEFAULT_SCROLL_RULE + " && " + KOTLIN_PLAYGROUND_SCROLL_RULE + ") {" +
                        "elem = elem.parentElement;" +
                     "}" +
                    HORIZONTAL_SCROLL_LISTENER + ".onScroll(elem.offsetWidth, elem.scrollWidth, elem.scrollLeft);" +
                "}" +
            "</script>\n";

    //string with 2 format args
    private static final String PRE_BODY = "<html>\n" +
            "<head>\n" +

            "<title>Step</title>\n" +

            "%s" +

            "%s" +

            "<meta name=\"viewport\" content=\"width=" +

            "%d" +

            ", user-scalable=no" +
            ", target-densitydpi=medium-dpi" +
            "\" />" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"wysiwyg.css\"/>" +
            HORIZONTAL_SCROLL_SCRIPT +
            HORIZONTAL_SCROLL_STYLE +
            "<base href=\"%s\">" +
            "</head>\n"
            + "<body style='margin:0;padding:0;'>";

    private static final String SelectionColorStyle =
            "<style>\n"
            + "::selection { background: #%06X; }\n"
            + "</style>";

    private static final float DefaultFontSize = 14f;

    private static final String DefaultFontStyle =
            "<style>\n"
            + "\nhtml{-webkit-text-size-adjust: 100%%;}"
            + "\nbody{font-size: %fpt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;}"
            + "\nh1{font-size: 20pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nh2{font-size: 17pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nh3{font-size: 14pt; font-family:Arial, Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nimg { max-width: 100%%; }"

            + "</style>\n";

    private static final String CustomFontStyle =
            "<style>\n" +
            "@font-face {" +
            "    font-family: 'Roboto';\n" +
            "    src: url(\"%s\")\n" +
            "}"
            + "\nhtml{-webkit-text-size-adjust: 100%%;}"
            + "\nbody{font-size: %fpx; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;}"
            + "\nh1{font-size: 22px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nh2{font-size: 19px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nh3{font-size: 16px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}"
            + "\nimg { max-width: 100%%; }"
            + "</style>\n";

    private static final String POST_BODY;

    private static final String MathJaxScript =
            "<script type=\"text/x-mathjax-config\">\n" +
                    "  MathJax.Hub.Config({" +
                    "showMathMenu: false, " +
                    "messageStyle: \"none\", " +
                    "TeX: {extensions: [ \"color.js\"]}, " +
                    "tex2jax: {preview: \"none\", inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
                    "displayMath: [ ['$$','$$'], ['\\[','\\]'] ]" +
                    "</script>\n" +
                    "<script type=\"text/javascript\"\n" +
                    " src=\"file:///android_asset/MathJax/MathJax.js?config=TeX-AMS_HTML\">\n" +
                    "</script>\n";

    public static String getUserPath(Config config, int userId) {
        return new StringBuilder()
                .append(config.getBaseUrl())
                .append(AppConstants.WEB_URI_SEPARATOR)
                .append("users")
                .append(AppConstants.WEB_URI_SEPARATOR)
                .append(userId)
                .append("/?from_mobile_app=true")
                .toString();
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
