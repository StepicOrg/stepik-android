package org.stepic.droid.store;

import org.stepic.droid.model.Step;

public interface IStoreStateManager {
    void updateUnitLessonState(long lessonId);

    void updateStepAfterDeleting(Step step);

    void updateSectionState(long sectionId);
}
