package org.stepik.android.view.base.ui.span;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TypefaceSpanCompat extends MetricAffectingSpan {

    @Nullable
    private final String mFamily;

    @Nullable
    private final Typeface mTypeface;

    /**
     * Constructs a {@link android.text.style.TypefaceSpan} based on the font family. The previous style of the
     * TextPaint is kept. If the font family is null, the text paint is not modified.
     *
     * @param family The font family for this typeface.  Examples include
     *               "monospace", "serif", and "sans-serif"
     */
    public TypefaceSpanCompat(@Nullable String family) {
        this(family, null);
    }

    /**
     * Constructs a {@link android.text.style.TypefaceSpan} from a {@link Typeface}. The previous style of the
     * TextPaint is overridden and the style of the typeface is used.
     *
     * @param typeface the typeface
     */
    public TypefaceSpanCompat(@Nullable Typeface typeface) {
        this(null, typeface);
    }

    private TypefaceSpanCompat(@Nullable String family, @Nullable Typeface typeface) {
        mFamily = family;
        mTypeface = typeface;
    }

    @Nullable
    public String getFamily() {
        return mFamily;
    }

    @Nullable
    public Typeface getTypeface() {
        return mTypeface;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        updateTypeface(ds);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        updateTypeface(paint);
    }

    private void updateTypeface(@NonNull Paint paint) {
        if (mTypeface != null) {
            paint.setTypeface(mTypeface);
        } else if (mFamily != null) {
            applyFontFamily(paint, mFamily);
        }
    }

    private void applyFontFamily(@NonNull Paint paint, @NonNull String family) {
        int style;
        Typeface old = paint.getTypeface();
        if (old == null) {
            style = Typeface.NORMAL;
        } else {
            style = old.getStyle();
        }
        final Typeface styledTypeface = Typeface.create(family, style);
        int fake = style & ~styledTypeface.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(styledTypeface);
    }
}