package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.Cursor;

import org.stepic.droid.store.structure.DBStructureCourses;

public final class DbOperationsCourses extends DbOperationsBase {
    public DbOperationsCourses(Context context) {
        super(context);
    }

    @Override
    public Cursor getCursor() {
        return database.query(DBStructureCourses.NAME, DBStructureCourses.getUsedColumns(),
                null, null, null, null, null);
    }
}
