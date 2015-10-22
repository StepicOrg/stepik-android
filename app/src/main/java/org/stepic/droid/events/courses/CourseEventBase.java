package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;

public abstract class CourseEventBase {
    DatabaseManager.Table mType;

    CourseEventBase(DatabaseManager.Table type) {
        mType = type;
    }

    public final DatabaseManager.Table getType() {
        return mType;
    }
}
