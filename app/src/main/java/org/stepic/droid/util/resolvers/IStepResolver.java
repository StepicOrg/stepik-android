package org.stepic.droid.util.resolvers;

import android.graphics.drawable.Drawable;

import org.stepic.droid.base.FragmentStepBase;
import org.stepic.droid.model.Step;

public interface IStepResolver {
    Drawable getDrawableForType(String type, boolean viewed);

    FragmentStepBase getFragment(Step step);

    boolean isViewedStatePost(Step step);
}
