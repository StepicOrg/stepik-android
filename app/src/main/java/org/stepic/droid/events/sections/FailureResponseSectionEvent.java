package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;

public class FailureResponseSectionEvent extends SectionBaseEvent{

    public FailureResponseSectionEvent(Course course) {
        super(course);
    }
}
