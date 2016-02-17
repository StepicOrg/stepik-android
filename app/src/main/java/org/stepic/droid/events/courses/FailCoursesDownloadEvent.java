package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public class FailCoursesDownloadEvent extends CourseEventBase {

    public FailCoursesDownloadEvent(DatabaseFacade.Table type) {
        super(type);
    }
}
