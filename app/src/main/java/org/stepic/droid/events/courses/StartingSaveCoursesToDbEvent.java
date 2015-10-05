package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class StartingSaveCoursesToDbEvent extends CourseEventBase {
    public StartingSaveCoursesToDbEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
