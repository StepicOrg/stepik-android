package org.stepic.droid.view.custom;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.stepic.droid.util.HtmlHelper;

public class LatexSupportableWebView extends WebView {

    private final static String assetUrl = "file:///android_asset/";

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

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((AppCompatActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;


        getSettings().setDomStorageEnabled(true);
        if (text.toString().contains("$")) {
            WebSettings webSettings = getSettings();
            webSettings.setJavaScriptEnabled(true);
            final String html = HtmlHelper.buildMathPage(text, width);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
                }
            }, 0);

        } else {
            final String html = HtmlHelper.buildPageWithAdjustingTextAndImage(text, width);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
                }
            }, 0);

        }
    }
}
