package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Progress;
import org.stepic.droid.store.structure.DbStructureProgress;

public class ProgressDaoImpl extends DaoBase<Progress> {


    public ProgressDaoImpl(SQLiteDatabase openHelper) {
        super(openHelper);
    }

    @Override
    public Progress parsePersistentObject(Cursor cursor) {
        Progress progress = new Progress();
        int indexId = cursor.getColumnIndex(DbStructureProgress.Column.ID);
        int indexCost = cursor.getColumnIndex(DbStructureProgress.Column.COST);
        int indexScore = cursor.getColumnIndex(DbStructureProgress.Column.SCORE);
        int indexIs_Passed = cursor.getColumnIndex(DbStructureProgress.Column.IS_PASSED);
        int indexLastViewed = cursor.getColumnIndex(DbStructureProgress.Column.LAST_VIEWED);
        int indexSteps = cursor.getColumnIndex(DbStructureProgress.Column.N_STEPS);
        int indexN_steps_passed = cursor.getColumnIndex(DbStructureProgress.Column.N_STEPS_PASSED);

        progress.setId(cursor.getString(indexId));
        progress.setCost(cursor.getInt(indexCost));
        progress.setScore(cursor.getString(indexScore));
        progress.set_passed(cursor.getInt(indexIs_Passed) > 0);
        progress.setLast_viewed(cursor.getString(indexLastViewed));
        progress.setN_steps(cursor.getInt(indexSteps));
        progress.setN_steps_passed(cursor.getInt(indexN_steps_passed));

        return progress;
    }

    @Override
    public String getDbName() {
        return DbStructureProgress.PROGRESS;
    }

    @Override
    public ContentValues getContentValues(Progress progress) {

        ContentValues values = new ContentValues();
        values.put(DbStructureProgress.Column.ID, progress.getId());
        values.put(DbStructureProgress.Column.COST, progress.getCost());
        values.put(DbStructureProgress.Column.SCORE, progress.getScore());
        values.put(DbStructureProgress.Column.IS_PASSED, progress.is_passed());
        values.put(DbStructureProgress.Column.LAST_VIEWED, progress.getLast_viewed());
        values.put(DbStructureProgress.Column.N_STEPS, progress.getN_steps());
        values.put(DbStructureProgress.Column.N_STEPS_PASSED, progress.getLast_viewed());
        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureProgress.Column.ID;
    }

    @Override
    public String getDefaultPrimaryValue(Progress persistentObject) {
        return persistentObject.getId();
    }

}
