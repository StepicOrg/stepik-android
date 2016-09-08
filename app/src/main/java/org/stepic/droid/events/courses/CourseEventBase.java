package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.Table;

public abstract class CourseEventBase {
    private Table mType;

    CourseEventBase(Table type) {
        mType = type;
    }

    public final Table getType() {
        return mType;
    }
}
