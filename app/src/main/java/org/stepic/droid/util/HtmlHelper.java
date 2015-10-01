package org.stepic.droid.util;

import android.text.Html;
import android.text.Spanned;

import org.jetbrains.annotations.Nullable;

public class HtmlHelper {

    public static Spanned fromHtml(@Nullable String content) {
        if (content == null)
            return Html.fromHtml("");

        return Html.fromHtml(content);
    }
}
