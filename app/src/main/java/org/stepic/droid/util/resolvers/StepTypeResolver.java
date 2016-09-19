package org.stepic.droid.util.resolvers;

import android.graphics.drawable.Drawable;

import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.model.Step;

public interface StepTypeResolver {
    Drawable getDrawableForType(String type, boolean viewed, boolean isPeerReview);

    StepBaseFragment getFragment(Step step);

    boolean isViewedStatePost(Step step);
}
