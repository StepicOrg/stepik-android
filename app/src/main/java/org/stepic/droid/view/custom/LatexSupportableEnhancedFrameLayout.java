package org.stepic.droid.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;

public class LatexSupportableEnhancedFrameLayout extends FrameLayout {
    private final static String assetUrl = "file:///android_asset/";
    TextView textView;
    LatexSupportableWebView webView;

    @ColorInt
    private final int themeDefaultTextColor;

    @ColorInt
    int textColor;

    @ColorInt
    int backgroundColor;

    @ColorInt
    int defaultTextColor;

    public LatexSupportableEnhancedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        defaultTextColor = ColorUtil.INSTANCE.getColorArgb(R.color.black, context);
        themeDefaultTextColor = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_regular_text, context);


        int[] set = {
                android.R.attr.textColor,
                android.R.attr.background
        };

        TypedArray ta = context.obtainStyledAttributes(attrs, set);
        try {
            textColor = ta.getColor(0, defaultTextColor);
            if (textColor == themeDefaultTextColor){
                textColor = defaultTextColor;
            }
            //noinspection ResourceType
            backgroundColor = ta.getColor(1, ColorUtil.INSTANCE.getColorArgb(R.color.transparent, context));
        } finally {
            ta.recycle();
        }

        init(context);

        textView.setTextColor(textColor);
        textView.setBackgroundColor(backgroundColor);

        webView.setBackgroundColor(backgroundColor);
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
            String coloredText = applyColoredWebView(text);
            textView.setVisibility(GONE);
            webView.setVisibility(VISIBLE);
            webView.setText(coloredText);
        }
    }

    public LatexSupportableWebView getWebView() {
        return webView;
    }

    private String applyColoredWebView(String text) {
        if (defaultTextColor != textColor) {
            String hexColor = String.format("#%06X", (0xFFFFFF & textColor));
            return "<br>" + "<font color='" + hexColor + "'>" + text + "</font>";
        } else {
            return text;
        }
    }

}
