package org.stepic.droid.events.courses;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseManager;

public class FailDropCourseEvent extends CourseEventBase {
    private final Course course;

    public Course getCourse() {
        return course;
    }


    public FailDropCourseEvent(DatabaseManager.Table type, Course course) {
        super(type);
        this.course = course;
    }
}
