package org.stepic.droid.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v4.widget.NestedScrollView;
import android.view.View;

public class DisplayUtils {
    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static boolean isVisible(NestedScrollView scrollView, View view) {
        Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);

        float top = view.getY();
        float bottom = top + view.getHeight();

        return scrollBounds.top <= top && scrollBounds.bottom >= bottom;
    }
}
