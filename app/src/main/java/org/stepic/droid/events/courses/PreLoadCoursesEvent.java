package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public class PreLoadCoursesEvent extends CourseEventBase {
    public PreLoadCoursesEvent(DatabaseFacade.Table type) {
        super(type);
    }
}
