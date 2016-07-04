package org.stepic.droid.events.courses;

public class CourseNotInDatabaseEvent {
    long courseId;

    public CourseNotInDatabaseEvent(long courseId) {
        this.courseId = courseId;
    }

    public long getCourseId() {
        return courseId;
    }
}
