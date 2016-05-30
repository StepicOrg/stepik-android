package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InterceptFrameLayout extends FrameLayout {
    public InterceptFrameLayout(Context context) {
        super(context, null);
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
