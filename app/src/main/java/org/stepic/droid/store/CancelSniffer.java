package org.stepic.droid.store;

public interface CancelSniffer {
    void addStepIdCancel(long stepId);

    void removeStepIdCancel(long stepId);

    boolean isStepIdCanceled(long stepId);

    void addSectionIdCancel(long sectionId);

    void removeSectionIdCancel(long sectionId);

    boolean isSectionIdIsCanceled(long sectionId);

    void addLessonToCancel(long lessonId);

    void removeLessonIdToCancel(long lessonId);

    boolean isLessonIdIsCanceled(long lessonId);
}
