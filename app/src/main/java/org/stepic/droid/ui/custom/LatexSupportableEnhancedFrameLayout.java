package org.stepic.droid.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.FontRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Px;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepic.droid.util.resolvers.text.TextResult;

import javax.inject.Inject;

@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class LatexSupportableEnhancedFrameLayout extends FrameLayout {
    private final static String assetUrl = "file:///android_asset/";
    private final static String fontsDirectory = "fonts/";
    private final static String fontFormat = ".ttf";
    private TextView textView;
    private LatexSupportableWebView webView;

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
        App.Companion.component().inject(this);


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
    }

    @LayoutRes
    @Contract(pure = true)
    protected int getViewRes() {
        return R.layout.latex_supportabe_enhanced_view;
    }

    @IdRes
    @Contract(pure = true)
    protected int getTextViewId() {
        return R.id.textView;
    }

    @IdRes
    @Contract(pure = true)
    protected int getWebViewId() {
        return R.id.webView;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(getViewRes(), this, true);
        textView = findViewById(getTextViewId());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        webView = findViewById(getWebViewId());
    }

    public void setLineHeight(@Px int lineHeight) {
        TextViewCompat.setLineHeight(textView, lineHeight);
    }

    public void setTextSize(float textSize) {
        textView.setTextSize(textSize);
        webView.setTextSize(textSize);
    }

    public void setTextIsSelectable(boolean isSelectable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) { // selection in WebView works incorrect on API <= 18, so its disabled
            textView.setTextIsSelectable(isSelectable);
            webView.setTextIsSelectable(isSelectable);
            textView.setMovementMethod(LinkMovementMethod.getInstance()); //fix opening links of the textview
        }
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
            setPlainText(textResult.getText());
        } else {
            setTextWebView(textResult.getText(), false, null);
        }
    }

    public LatexSupportableWebView getWebView() {
        return webView;
    }

    private void setTextWebView(CharSequence text, boolean wantLaTeX, @FontRes Integer fontResId) {
        textView.setVisibility(GONE);
        webView.setVisibility(VISIBLE);
        webView.setText(text, wantLaTeX, fontResId != null ? assetUrl + fontsDirectory + getResources().getResourceEntryName(fontResId) + fontFormat : null);
    }

    private void setTextWebViewOnlyForLaTeX(String text) {
        setTextWebView(text, true, null);
    }

    private void setPlainText(CharSequence text) {
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
        setPlainOrLaTeXTextWithCustomFontColored(text, null, colorRes, true);
    }

    public void setPlainOrLaTeXTextWithCustomFontColored(String text, @FontRes Integer fontResId, @ColorRes int colorRes, boolean allowLaTeX) {
        @ColorInt
        int colorArgb = ColorUtil.INSTANCE.getColorArgb(colorRes, getContext());
        TextResult textResult = textResolver.resolveStepText(text);
        if (textResult.isNeedWebView()) {
            String hexColor = String.format("#%06X", (0xFFFFFF & colorArgb));
            String coloredText = "<font color='" + hexColor + "'>" + textResult.getText() + "</font>";
            setTextWebView(coloredText, allowLaTeX && HtmlHelper.hasLaTeX(text), fontResId);
        } else {
            textView.setTextColor(colorArgb);
            setPlainText(textResult.getText());
            if (fontResId != null) {
                textView.setTypeface(ResourcesCompat.getFont(getContext(), fontResId));
            }
        }
    }
}
