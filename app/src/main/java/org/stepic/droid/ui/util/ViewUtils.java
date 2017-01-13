package org.stepic.droid.ui.util;

import android.support.v4.view.ViewCompat;
import android.view.View;

import timber.log.Timber;

public class ViewUtils {
    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        boolean isHit = (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
        Timber.d("hit in view %s", isHit);
        return isHit;
    }

}
