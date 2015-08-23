package com.elpatika.stepic.view.custom;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.elpatika.stepic.R;
import com.elpatika.stepic.view.custom.FontFactory;

public class STextView extends TextView {

    public STextView(Context context) {
        super(context);
    }

    public STextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context, attrs);
    }

    public STextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttrs(context, attrs);
    }

    private void processAttrs(Context context, AttributeSet attrs) {
        if(isInEditMode())
            return;

//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.custom_view, 0, 0);

        try {
            // check for the font attribute and setup font
            String fontFileName = "OpenSans-Regular.ttf";

            Typeface font = FontFactory.getInstance().getFont(context,fontFileName);
            setTypeface(font);
        } catch (Exception ex) {
        } finally {
            //  a.recycle();
        }
    }
}
