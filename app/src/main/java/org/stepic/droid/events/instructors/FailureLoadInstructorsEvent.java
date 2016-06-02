package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;

public class FailureLoadInstructorsEvent extends InstructorsBaseEvent {
    private final Throwable t;

    public FailureLoadInstructorsEvent(Course mCourse, Throwable t) {
        super(mCourse);
        this.t = t;
    }
}
