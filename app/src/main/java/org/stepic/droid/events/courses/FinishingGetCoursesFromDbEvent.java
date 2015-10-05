package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class FinishingGetCoursesFromDbEvent extends CourseEventBase {
    public FinishingGetCoursesFromDbEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
