package org.stepic.droid.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.yandex.metrica.YandexMetrica;


public class SEditTextView extends EditText {
    public SEditTextView(Context context) {
        super(context);
    }

    public SEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context, attrs);
    }

    public SEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttrs(context, attrs);
    }

    private void processAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, org.stepic.droid.R.styleable.custom_view, 0, 0);

        try {
            String fontFileName = a.getString(org.stepic.droid.R.styleable.custom_view_font);
            Typeface font = FontFactory.getInstance().getFont(context,
                    fontFileName);
            setTypeface(font);
        } catch (Exception ignored) {

            YandexMetrica.reportError("customEditText", ignored);

        } finally {
            a.recycle();
        }
    }
}
