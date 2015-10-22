package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public class StartingSaveCoursesToDbEvent extends CourseEventBase {
    public StartingSaveCoursesToDbEvent(DatabaseManager.Table type) {
        super(type);
    }
}
