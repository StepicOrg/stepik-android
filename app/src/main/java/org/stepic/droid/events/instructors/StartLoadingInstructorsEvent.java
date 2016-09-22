package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;

public class StartLoadingInstructorsEvent extends InstructorsBaseEvent {
    public StartLoadingInstructorsEvent(Course course) {
        super(course);
    }
}
