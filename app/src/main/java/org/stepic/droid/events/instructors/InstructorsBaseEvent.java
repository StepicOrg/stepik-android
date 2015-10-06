package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;

public class InstructorsBaseEvent {
    final Course mCourse;

    public InstructorsBaseEvent(Course mCourse) {
        this.mCourse = mCourse;
    }

    public Course getCourse() {
        return mCourse;
    }
}
