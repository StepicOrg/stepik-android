package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public class StartingGetCoursesFromDbEvent extends CourseEventBase{
    public StartingGetCoursesFromDbEvent(DatabaseFacade.Table type) {
        super(type);
    }
}
