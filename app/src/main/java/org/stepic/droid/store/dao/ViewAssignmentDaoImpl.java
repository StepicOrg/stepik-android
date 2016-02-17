package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.store.structure.DbStructureViewQueue;
import org.stepic.droid.web.ViewAssignment;

public class ViewAssignmentDaoImpl extends DaoBase<ViewAssignment> {
    public ViewAssignmentDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
    }

    @Override
    public ViewAssignment parsePersistentObject(Cursor cursor) {
        int indexStepId = cursor.getColumnIndex(DbStructureViewQueue.Column.STEP_ID);
        int indexAssignmentId = cursor.getColumnIndex(DbStructureViewQueue.Column.ASSIGNMENT_ID);


        long stepId = cursor.getLong(indexStepId);
        long assignmentId = cursor.getLong(indexAssignmentId);

        return new ViewAssignment(assignmentId, stepId);
    }

    @Override
    public String getDbName() {
        return DbStructureViewQueue.VIEW_QUEUE;
    }

    @Override
    public ContentValues getContentValues(ViewAssignment viewState) {
        ContentValues values = new ContentValues();

        values.put(DbStructureViewQueue.Column.ASSIGNMENT_ID, viewState.getAssignment());
        values.put(DbStructureViewQueue.Column.STEP_ID, viewState.getStep());

        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureViewQueue.Column.STEP_ID;
    }

    @Override
    public String getDefaultPrimaryValue(ViewAssignment persistentObject) {
        return persistentObject.getStep() + "";
    }
}
