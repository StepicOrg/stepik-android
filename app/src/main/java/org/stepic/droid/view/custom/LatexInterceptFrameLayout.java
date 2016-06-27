package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LatexInterceptFrameLayout extends LatexSupportableEnhancedFrameLayout {
    public LatexInterceptFrameLayout(Context context) {
        super(context, null);
    }

    public LatexInterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
