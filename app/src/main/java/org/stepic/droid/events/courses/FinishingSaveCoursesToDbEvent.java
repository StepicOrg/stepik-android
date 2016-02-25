package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public class FinishingSaveCoursesToDbEvent extends CourseEventBase {
    public FinishingSaveCoursesToDbEvent(DatabaseFacade.Table type) {
        super(type);
    }
}
