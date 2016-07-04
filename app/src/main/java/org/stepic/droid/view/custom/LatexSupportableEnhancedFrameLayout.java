package org.stepic.droid.view.custom;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.util.HtmlHelper;

public class LatexSupportableEnhancedFrameLayout extends FrameLayout {
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
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        webView = (LatexSupportableWebView) findViewById(R.id.webView);
    }

    public void setText(String text) {
        boolean needWebView = HtmlHelper.isForWebView(text); //text is raw text from response
        if (!needWebView) {
            CharSequence str = HtmlHelper.trimTrailingWhitespace(HtmlHelper.fromHtml(text));
            webView.setVisibility(GONE);
            textView.setVisibility(VISIBLE);
            textView.setText(str);
        } else {
            textView.setVisibility(GONE);
            webView.setVisibility(VISIBLE);
            webView.setText(text);
        }
    }

    public LatexSupportableWebView getWebView() {
        return webView;
    }
}
