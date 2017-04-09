package org.stepic.droid.storage;

import org.stepic.droid.model.Step;

public interface StoreStateManager {

    void updateUnitLessonState(long lessonId);

    void updateUnitLessonAfterDeleting(long lessonId);

    void updateStepAfterDeleting(Step step);

    void updateSectionAfterDeleting(long sectionId);

    void updateSectionState(long sectionId);
}
