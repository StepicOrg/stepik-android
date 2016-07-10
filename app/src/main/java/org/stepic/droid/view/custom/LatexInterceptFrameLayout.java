package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;

public class LatexInterceptFrameLayout extends LatexSupportableEnhancedFrameLayout {
    public LatexInterceptFrameLayout(Context context) {
        super(context, null);
    }

    public LatexInterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
//        setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return true;
//            }
//        });
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
//    }
}
