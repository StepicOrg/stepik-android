package org.stepic.droid.ui.util;

import androidx.core.view.ViewCompat;
import android.view.View;

import org.jetbrains.annotations.Nullable;

public class ViewUtils {
    public static boolean hitTest(@Nullable View v, int x, int y) {
        if (v == null){
            return  false;
        }

        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

}
