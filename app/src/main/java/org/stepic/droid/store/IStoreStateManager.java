package org.stepic.droid.store;

import org.stepic.droid.model.Unit;

public interface IStoreStateManager {
    void updateUnitLessonState(long lessonId);
    void updateUnitLessonAfterDeleting (Unit unit);
}
