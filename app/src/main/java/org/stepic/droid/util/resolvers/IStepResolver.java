package org.stepic.droid.util.resolvers;

import android.graphics.drawable.Drawable;

public interface IStepResolver {
    Drawable getDrawableForType(String type, boolean viewed);
}
