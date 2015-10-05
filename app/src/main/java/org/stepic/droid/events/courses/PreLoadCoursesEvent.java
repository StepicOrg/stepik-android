package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class PreLoadCoursesEvent extends CourseEventBase {
    public PreLoadCoursesEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
