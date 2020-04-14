package org.stepic.droid.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.HtmlCompat;

import org.stepic.droid.R;

public class ExpandableTextView extends AppCompatTextView {
    private static final int DEFAULT_TRIM_LENGTH = 200;
    private final String ELLIPSIS;
    private CharSequence originalText;
    private CharSequence trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            ELLIPSIS = "<font color=" + "#CCCCCC" + ">" + "more..." + "</font>";
        } else {
            ELLIPSIS = "<font color=" + getResources().getColor(R.color.color_secondary) + ">" + context.getString(R.string.tap_to_see_more) + "</font>";
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
        typedArray.recycle();

        setOnClickListener(v -> {
            trim = !trim;
            setText();
//                requestFocusFromTouch();
        });
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText(text);
        bufferType = type;
        setText();
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (originalText != null && originalText.length() > trimLength) {
            return new SpannableStringBuilder(originalText, 0, trimLength + 1)
                    .append(" ")
                    .append(HtmlCompat.fromHtml(ELLIPSIS, HtmlCompat.FROM_HTML_MODE_COMPACT));
        } else {
            return originalText;
        }
    }

    public CharSequence getOriginalText() {
        return originalText;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        trimmedText = getTrimmedText(originalText);
        setText();
    }

    public int getTrimLength() {
        return trimLength;
    }
}