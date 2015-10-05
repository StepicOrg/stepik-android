package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class FailCoursesDownloadEvent extends CourseEventBase {

    public FailCoursesDownloadEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
