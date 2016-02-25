package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseFacade;

public abstract class CourseEventBase {
    private DatabaseFacade.Table mType;

    CourseEventBase(DatabaseFacade.Table type) {
        mType = type;
    }

    public final DatabaseFacade.Table getType() {
        return mType;
    }
}
