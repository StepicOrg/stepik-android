package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.stepic.droid.util.AppConstants;

public class LatexSupportableWebView extends WebView {

    private String assetUrl;

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

    private void init() {
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    public void setText(CharSequence text) {

        final String mimeType = "text/html";
        final String encoding = "UTF-8";
        setBackgroundColor(0);
        if (text.toString().contains("$")) {
            WebSettings webSettings = getSettings();
            webSettings.setJavaScriptEnabled(true);

            final String html = AppConstants.PRE_BODY + text + AppConstants.POST_BODY;

            assetUrl = "file:///android_asset/";
            loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
        } else {
            loadDataWithBaseURL(assetUrl, text.toString(), mimeType, encoding, "");
        }
    }
}
