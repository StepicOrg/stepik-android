package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DbOperationsCourses;

public abstract class CourseEventBase {
    DbOperationsCourses.Table mType;

    CourseEventBase(DbOperationsCourses.Table type) {
        mType = type;
    }

    public final DbOperationsCourses.Table getType() {
        return mType;
    }
}
