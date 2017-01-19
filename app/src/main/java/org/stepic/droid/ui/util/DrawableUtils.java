package org.stepic.droid.ui.util;


import android.graphics.drawable.Drawable;

public class DrawableUtils {
    private static final int[] EMPTY_STATE = new int[]{};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }
}