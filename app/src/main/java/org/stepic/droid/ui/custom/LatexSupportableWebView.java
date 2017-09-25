package org.stepic.droid.ui.custom;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.ui.util.AssetSupportWebViewClient;
import org.stepic.droid.util.HtmlHelper;

import java.util.Calendar;

import javax.inject.Inject;

public class LatexSupportableWebView extends WebView implements View.OnClickListener, View.OnTouchListener {
    private static final String mimeType = "text/html";
    private static final String encoding = "UTF-8";

    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;
    OnWebViewImageClicked listener;

    @Inject
    Config config;

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
        App.Companion.component().inject(this);
        setBackgroundColor(Color.TRANSPARENT);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        setOnClickListener(this);
        setOnTouchListener(this);
    }


    public void setText(CharSequence text) {
        setText(text, false); //by default we do not want latex -> try to optimize
    }

    public void setText(CharSequence text, boolean wantLaTeX) {
        setText(text, wantLaTeX, null);
    }

    public void setText(CharSequence text, boolean wantLaTeX, String fontPath) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((AppCompatActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;


        getSettings().setDomStorageEnabled(true);
        String textString = text.toString();

        final String html;
        WebSettings webSettings = getSettings();
        if (fontPath != null) {
            webSettings.setJavaScriptEnabled(true);
            html = HtmlHelper.buildPageWithCustomFont(text, fontPath, width, config.getBaseUrl());
        } else if (wantLaTeX || HtmlHelper.hasLaTeX(textString)) {
            webSettings.setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                setWebViewClient(new AssetSupportWebViewClient());
            }
            html = HtmlHelper.buildMathPage(text, width, config.getBaseUrl());
        } else {
            html = HtmlHelper.buildPageWithAdjustingTextAndImage(text, width, config.getBaseUrl());
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
            }
        }, 0);
    }


    @Override
    public void onClick(View v) {
        WebView.HitTestResult hr = getHitTestResult();
        try {
            if (listener != null && hr.getType() == HitTestResult.IMAGE_TYPE) {
                listener.onClick(hr.getExtra());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    performClick();
                }
            }
        }
        return false;
    }


    public void setOnWebViewClickListener(OnWebViewImageClicked listener) {
        this.listener = listener;
    }

    interface OnWebViewImageClicked {
        void onClick(String path);
    }
}
