package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public class FinishingSaveCoursesToDbEvent extends CourseEventBase {
    public FinishingSaveCoursesToDbEvent(DatabaseManager.Table type) {
        super(type);
    }
}
