package org.stepic.droid.events.courses;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseFacade;

public class SuccessDropCourseEvent extends CourseEventBase {
    private final Course course;

    public SuccessDropCourseEvent(DatabaseFacade.Table type, Course course) {
        super(type);
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }
}