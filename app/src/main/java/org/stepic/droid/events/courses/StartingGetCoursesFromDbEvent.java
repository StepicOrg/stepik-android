package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public class StartingGetCoursesFromDbEvent extends CourseEventBase{
    public StartingGetCoursesFromDbEvent(DatabaseManager.Table type) {
        super(type);
    }
}
