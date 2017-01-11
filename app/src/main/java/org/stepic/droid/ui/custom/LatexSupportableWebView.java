package org.stepic.droid.ui.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.util.HtmlHelper;

import java.util.Calendar;

import javax.inject.Inject;

public class LatexSupportableWebView extends WebView implements View.OnClickListener, View.OnTouchListener {

    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;
    OnWebViewImageClicked listener;

    @Inject
    IConfig config;

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
        MainApplication.component().inject(this);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        setBackgroundColor(Color.TRANSPARENT);

        setOnClickListener(this);
        setOnTouchListener(this);
    }


    public void setText(CharSequence text) {
        setText(text, false); //by default we do not want latex -> try to optimize
    }

    public void setText(CharSequence text, boolean wantLaTeX) {

        final String mimeType = "text/html";
        final String encoding = "UTF-8";

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((AppCompatActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;


        getSettings().setDomStorageEnabled(true);
        String textString = text.toString();
        if (wantLaTeX || HtmlHelper.hasLaTeX(textString)) {
            WebSettings webSettings = getSettings();
            webSettings.setJavaScriptEnabled(true);
            final String html = HtmlHelper.buildMathPage(text, width, config.getBaseUrl());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
                }
            }, 0);

        } else {
            final String html = HtmlHelper.buildPageWithAdjustingTextAndImage(text, width, config.getBaseUrl());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(assetUrl, html, mimeType, encoding, "");
                }
            }, 0);

        }
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
