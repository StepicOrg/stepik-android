package org.stepic.droid.store;

public interface ICancelSniffer {
    void addStepIdCancel(long stepId);

    void removeStepIdCancel(long stepId);

    boolean isStepIdCanceled(long stepId);

    void addSectionIdCancel(long sectionId);

    void removeSectionIdCancel(long sectionId);

    boolean isSectionIdIsCanceled(long sectionId);

    void addUnitIdCancel(long unitId);

    void removeUnitIdCancel(long unitId);

    boolean isUnitIdIsCanceled(long unitId);
}
