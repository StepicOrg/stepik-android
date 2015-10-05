package org.stepic.droid.events.courses;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

public class GettingCoursesFromDbSuccessEvent extends CourseEventBase {
    public List<Course> getCourses() {
        return courses;
    }

    private final List<Course> courses;

    public GettingCoursesFromDbSuccessEvent(DbOperationsCourses.Table type, List<Course> courses) {
        super(type);
        this.courses = courses;
    }
}
