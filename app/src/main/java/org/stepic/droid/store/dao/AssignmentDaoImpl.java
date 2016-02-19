package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Assignment;
import org.stepic.droid.store.structure.DbStructureAssignment;

public class AssignmentDaoImpl extends DaoBase<Assignment> {
    public AssignmentDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
    }

    @Override
    public Assignment parsePersistentObject(Cursor cursor) {
        Assignment assignment = new Assignment();

        int columnIndexAssignmentId = cursor.getColumnIndex(DbStructureAssignment.Column.ASSIGNMENT_ID);
        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureAssignment.Column.CREATE_DATE);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureAssignment.Column.PROGRESS);
        int columnIndexStepId = cursor.getColumnIndex(DbStructureAssignment.Column.STEP_ID);
        int columnIndexUnitId = cursor.getColumnIndex(DbStructureAssignment.Column.UNIT_ID);
        int columnIndexUpdateDate = cursor.getColumnIndex(DbStructureAssignment.Column.UPDATE_DATE);

        assignment.setCreate_date(cursor.getString(columnIndexCreateDate));
        assignment.setId(cursor.getLong(columnIndexAssignmentId));
        assignment.setProgress(cursor.getString(columnIndexProgress));
        assignment.setStep(cursor.getLong(columnIndexStepId));
        assignment.setUnit(cursor.getLong(columnIndexUnitId));
        assignment.setUpdate_date(cursor.getString(columnIndexUpdateDate));
        return assignment;
    }

    @Override
    public String getDbName() {
        return DbStructureAssignment.ASSIGNMENTS;
    }

    @Override
    public ContentValues getContentValues(Assignment assignment) {
        ContentValues values = new ContentValues();

        values.put(DbStructureAssignment.Column.ASSIGNMENT_ID, assignment.getId());
        values.put(DbStructureAssignment.Column.CREATE_DATE, assignment.getCreate_date());
        values.put(DbStructureAssignment.Column.PROGRESS, assignment.getProgressId());
        values.put(DbStructureAssignment.Column.STEP_ID, assignment.getStep());
        values.put(DbStructureAssignment.Column.UNIT_ID, assignment.getUnit());
        values.put(DbStructureAssignment.Column.UPDATE_DATE, assignment.getUpdate_date());

        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureAssignment.Column.ASSIGNMENT_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Assignment persistentObject) {
        return persistentObject.getId()+"";
    }
}
