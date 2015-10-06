package org.stepic.droid.events.courses;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

public class FinishingGetCoursesFromDbEvent extends CourseEventBase {
    private final List<Course> result;

    public FinishingGetCoursesFromDbEvent(DbOperationsCourses.Table type, List<Course> result) {
        super(type);
        this.result = result;
    }

    public List<Course> getResult() {
        return result;
    }
}
