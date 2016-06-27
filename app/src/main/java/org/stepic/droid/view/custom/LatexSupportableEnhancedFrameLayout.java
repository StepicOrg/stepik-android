package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.util.HtmlHelper;

public class LatexSupportableEnhancedFrameLayout extends InterceptFrameLayout {
    private final static String assetUrl = "file:///android_asset/";


    TextView textView;
    LatexSupportableWebView webView;

    public LatexSupportableEnhancedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.latex_supportabe_enhanced_view, this, true);
        textView = (TextView) findViewById(R.id.textView);
        webView = (LatexSupportableWebView) findViewById(R.id.webView);
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
