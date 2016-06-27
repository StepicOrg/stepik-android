package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;

public class LatexSupportableEnhancedFrameLayout extends InterceptFrameLayout {
    private final static String assetUrl = "file:///android_asset/";


    TextView textView;
    LatexSupportableWebView webView;

    public LatexSupportableEnhancedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
//        setLayoutParamForView(this,  ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        textView = new TextView(getContext());
        textView.setTextColor(ColorUtil.INSTANCE.getColorArgb(R.color.black, getContext()));
        setLayoutParamForView(textView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setGravity(Gravity.START);//todo: really need?
        textView.setVisibility(GONE);
        addView(textView);

        webView = new LatexSupportableWebView(getContext());
        webView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        setLayoutParamForView(webView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        webView.setVisibility(GONE);
        addView(webView);
    }

    private void setLayoutParamForView(View targetView, int width, int height) {
//        ViewGroup.LayoutParams taggetViewLp = targetView.getLayoutParams();
//        taggetViewLp.width = width;
//        taggetViewLp.height = height;
//        targetView.setLayoutParams(taggetViewLp);
    }

    public void setText(String text) {
        boolean needWebView = HtmlHelper.isForWebView(text); //text is raw text from response
        if (!needWebView) {
            String str = HtmlHelper.trimTrailingWhitespace(HtmlHelper.fromHtml(text)).toString();
            webView.setVisibility(GONE);
            textView.setVisibility(VISIBLE);
            textView.setText(str);
        } else {
            textView.setVisibility(GONE);
            webView.setVisibility(VISIBLE);
            webView.setText(text);
        }
    }
}
