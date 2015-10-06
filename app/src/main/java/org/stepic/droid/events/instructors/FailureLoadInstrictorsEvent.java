package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;

public class FailureLoadInstrictorsEvent extends InstructorsBaseEvent {
    private final Throwable t;

    public FailureLoadInstrictorsEvent(Course mCourse, Throwable t) {
        super(mCourse);
        this.t = t;
    }

    public Throwable getT() {
        return t;
    }
}
