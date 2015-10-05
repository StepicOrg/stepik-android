package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;

public class FailureResponseSectionEvent {
    private final Course mCourse;

    public FailureResponseSectionEvent(Course mCourse) {
        this.mCourse = mCourse;
    }

    public Course getCourse() {
        return mCourse;
    }
}
