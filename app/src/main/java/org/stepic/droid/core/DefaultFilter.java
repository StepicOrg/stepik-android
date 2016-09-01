package org.stepic.droid.core;

import org.stepic.droid.model.StepikFilter;

public interface DefaultFilter {
    boolean getDefaultFeatured(StepikFilter filterValue);

    boolean getDefaultEnrolled(StepikFilter filterValue);
}
