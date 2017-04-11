package org.stepic.droid.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepic.droid.util.resolvers.text.TextResult;

import javax.inject.Inject;

public class LatexSupportableEnhancedFrameLayout extends FrameLayout {
    private final static String assetUrl = "file:///android_asset/";
    TextView textView;
    LatexSupportableWebView webView;

    @ColorInt
    int backgroundColor;

    @Inject
    TextResolver textResolver;

    @Inject
    ScreenManager screenManager;

    public LatexSupportableEnhancedFrameLayout(Context context) {
        this(context, null);
    }

    public LatexSupportableEnhancedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        App.component().inject(this);


        int[] set = {
                android.R.attr.background
        };

        TypedArray ta = context.obtainStyledAttributes(attrs, set);
        try {
            //noinspection ResourceType
            backgroundColor = ta.getColor(0, ColorUtil.INSTANCE.getColorArgb(R.color.transparent, context));
        } finally {
            ta.recycle();
        }

        init(context);

        textView.setBackgroundColor(backgroundColor);
        webView.setBackgroundColor(backgroundColor);

        textView.setTextIsSelectable(true);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.latex_supportabe_enhanced_view, this, true);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        webView = (LatexSupportableWebView) findViewById(R.id.webView);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        webView.setOnWebViewClickListener(new LatexSupportableWebView.OnWebViewImageClicked() {
            @Override
            public void onClick(String path) {
                screenManager.openImage(LatexSupportableEnhancedFrameLayout.this.getContext(), path);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webView.setOnWebViewClickListener(null);
    }

    public void setText(String text) {
        TextResult textResult = textResolver.resolveStepText(text);
        if (!textResult.isNeedWebView()) {
            webView.setVisibility(GONE);
            textView.setVisibility(VISIBLE);
            textView.setText(textResult.getText());
        } else {
            textView.setVisibility(GONE);
            webView.setVisibility(VISIBLE);
            webView.setText(textResult.getText());
        }
    }

    public LatexSupportableWebView getWebView() {
        return webView;
    }

    private void setTextWebViewOnlyForLaTeX(String text) {
        textView.setVisibility(GONE);
        webView.setVisibility(VISIBLE);
        webView.setText(text, true);
    }

    private void setPlainText(String text) {
        webView.setVisibility(GONE);
        textView.setVisibility(VISIBLE);
        textView.setText(text);
    }

    public void setPlainOrLaTeXText(String text) {
        if (HtmlHelper.hasLaTeX(text)) {
            setTextWebViewOnlyForLaTeX(text);
        } else {
            setPlainText(text);
        }
    }

    public void setPlainOrLaTeXTextColored(String text, @ColorRes int colorRes) {
        @ColorInt
        int colorArgb = ColorUtil.INSTANCE.getColorArgb(colorRes, getContext());
        if (HtmlHelper.hasLaTeX(text)) {
            String hexColor = String.format("#%06X", (0xFFFFFF & colorArgb));
            String coloredText = "<font color='" + hexColor + "'>" + text + "</font>";
            setTextWebViewOnlyForLaTeX(coloredText);
        } else {
            textView.setTextColor(colorArgb);
            setPlainText(text);
            Linkify.addLinks(textView, Linkify.ALL);
            textView.setLinksClickable(true);
        }
    }
}
