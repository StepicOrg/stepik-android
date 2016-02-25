package org.stepic.droid.store;

public interface ICancelSniffer {
    void addStepIdCancel(long stepId);

    void removeStepIdCancel(long stepId);

    boolean isStepIdCanceled(long stepId);
}
