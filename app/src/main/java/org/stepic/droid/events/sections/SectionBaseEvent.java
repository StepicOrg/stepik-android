package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;

public class SectionBaseEvent {
    final Course mCourse;

    public SectionBaseEvent (Course course)
    {
        mCourse = course;
    }

    public Course getCourse() {
        return mCourse;
    }
}
