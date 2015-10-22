package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public class PreLoadCoursesEvent extends CourseEventBase {
    public PreLoadCoursesEvent(DatabaseManager.Table type) {
        super(type);
    }
}
