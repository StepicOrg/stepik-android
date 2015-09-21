package org.stepic.droid.view.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class STextView extends TextView {

    public STextView(Context context) {
        super(context);
    }

    public STextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context);
    }

    public STextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttrs(context);
    }

    private void processAttrs(Context context) {
        if (isInEditMode())
            return;
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.custom_view, 0, 0);

        try {
            // check for the font attribute and setup font
            String fontFileName = "OpenSans-Regular.ttf";

            Typeface font = FontFactory.getInstance().getFont(context, fontFileName);
            setTypeface(font);
        } catch (Exception ignored) {
        }
    }
}
