package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;

public class StartingGetSectionFromDbEvent extends SectionBaseEvent {
    public StartingGetSectionFromDbEvent(Course course) {
        super(course);
    }
}
