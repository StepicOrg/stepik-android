package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class FinishingSaveCoursesToDbEvent extends CourseEventBase {
    public FinishingSaveCoursesToDbEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
