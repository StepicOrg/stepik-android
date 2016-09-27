package org.stepic.droid.util.resolvers.text;

import android.os.Build;
import android.text.Html;

public class Lol
{
    public static void main(String[] args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml("wa", Html.FROM_HTML_MODE_LEGACY, null, null);
        }
    }
}
