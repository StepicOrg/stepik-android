package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public class StartingSaveCoursesToDbEvent extends CourseEventBase {
    public StartingSaveCoursesToDbEvent(DatabaseFacade.Table type) {
        super(type);
    }
}
