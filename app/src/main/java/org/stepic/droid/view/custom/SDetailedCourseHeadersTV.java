package org.stepic.droid.view.custom;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import org.stepic.droid.R;

public class SDetailedCourseHeadersTV extends STextView {
    public static final int PADDING_DP = 2;


    public SDetailedCourseHeadersTV(Context context) {
        super(context);
        init(context);
    }

    public SDetailedCourseHeadersTV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SDetailedCourseHeadersTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private int getPaddingInPixels() {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (PADDING_DP * scale + 0.5f);
    }

    private void init(Context context) {
        Resources resources = getResources();
        int colorInt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorInt = resources.getColor(R.color.stepic_regular_text, null);
        } else {
            colorInt = resources.getColor(R.color.stepic_regular_text);
        }
        setTextColor(colorInt);

        setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_header_detailed));

        int pixelPadding = getPaddingInPixels();
        setPadding(0, pixelPadding, 0, pixelPadding);

    }
}
