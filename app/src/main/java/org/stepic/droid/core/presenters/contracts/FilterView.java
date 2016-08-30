package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.StepikFilter;

import java.util.EnumSet;

public interface FilterView {
    void onFilterAccepted();
    void onFiltersPreparedForView(EnumSet<StepikFilter> filters);
}