package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class LatexSupportableWebView extends WebView {
    public LatexSupportableWebView(Context context) {
        this(context, null);
    }

    public LatexSupportableWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LatexSupportableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init () {

    }
}
