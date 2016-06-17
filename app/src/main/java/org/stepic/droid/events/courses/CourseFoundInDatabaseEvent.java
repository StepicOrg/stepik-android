package org.stepic.droid.events.courses;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;

public class CourseFoundInDatabaseEvent {
    Course course;

    public CourseFoundInDatabaseEvent(@NotNull Course course) {
        this.course = course;
    }

    @NotNull
    public Course getCourse() {
        return course;
    }
}
