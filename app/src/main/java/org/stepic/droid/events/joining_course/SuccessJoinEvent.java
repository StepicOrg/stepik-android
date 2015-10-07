package org.stepic.droid.events.joining_course;

import org.stepic.droid.model.Course;

public class SuccessJoinEvent {
    private Course mCourse;

    public SuccessJoinEvent(Course mCourse) {

        this.mCourse = mCourse;
    }

    public Course getCourse() {
        return mCourse;
    }
}
