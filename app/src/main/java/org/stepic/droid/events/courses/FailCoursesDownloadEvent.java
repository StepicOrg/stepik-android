package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public class FailCoursesDownloadEvent extends CourseEventBase {

    public FailCoursesDownloadEvent(DatabaseManager.Table type) {
        super(type);
    }
}
