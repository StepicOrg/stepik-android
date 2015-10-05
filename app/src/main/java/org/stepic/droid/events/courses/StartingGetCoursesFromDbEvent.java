package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class StartingGetCoursesFromDbEvent extends CourseEventBase{
    public StartingGetCoursesFromDbEvent(DbOperationsCourses.Table type) {
        super(type);
    }
}
