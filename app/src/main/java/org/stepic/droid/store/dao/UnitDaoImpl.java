package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Unit;
import org.stepic.droid.store.structure.DbStructureUnit;
import org.stepic.droid.util.DbParseHelper;

public class UnitDaoImpl extends DaoBase<Unit> {


    public UnitDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
    }

    @Override
    public Unit parsePersistentObject(Cursor cursor) {
        throw new RuntimeException(); // TODO: 16.02.16 make it
    }

    @Override
    public String getDbName() {
        return DbStructureUnit.UNITS;
    }

    @Override
    public ContentValues getContentValues(Unit unit) {
        ContentValues values = new ContentValues();
        values.put(DbStructureUnit.Column.UNIT_ID, unit.getId());
        values.put(DbStructureUnit.Column.SECTION, unit.getSection());
        values.put(DbStructureUnit.Column.LESSON, unit.getLesson());
        values.put(DbStructureUnit.Column.ASSIGNMENTS, DbParseHelper.parseLongArrayToString(unit.getAssignments()));
        values.put(DbStructureUnit.Column.POSITION, unit.getPosition());
        values.put(DbStructureUnit.Column.PROGRESS, unit.getProgress());
        values.put(DbStructureUnit.Column.BEGIN_DATE, unit.getBegin_date());
        values.put(DbStructureUnit.Column.END_DATE, unit.getEnd_date());
        values.put(DbStructureUnit.Column.SOFT_DEADLINE, unit.getSoft_deadline());
        values.put(DbStructureUnit.Column.HARD_DEADLINE, unit.getHard_deadline());
        values.put(DbStructureUnit.Column.GRADING_POLICY, unit.getGrading_policy());
        values.put(DbStructureUnit.Column.BEGIN_DATE_SOURCE, unit.getBegin_date_source());
        values.put(DbStructureUnit.Column.END_DATE_SOURCE, unit.getEnd_date_source());
        values.put(DbStructureUnit.Column.SOFT_DEADLINE_SOURCE, unit.getSoft_deadline_source());
        values.put(DbStructureUnit.Column.HARD_DEADLINE_SOURCE, unit.getHard_deadline_source());
        values.put(DbStructureUnit.Column.GRADING_POLICY_SOURCE, unit.getGrading_policy_source());
        values.put(DbStructureUnit.Column.IS_ACTIVE, unit.is_active());
        values.put(DbStructureUnit.Column.CREATE_DATE, unit.getCreate_date());
        values.put(DbStructureUnit.Column.UPDATE_DATE, unit.getUpdate_date());
        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
       return DbStructureUnit.Column.UNIT_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Unit persistentObject) {
        return persistentObject.getId() + "";
    }


}
