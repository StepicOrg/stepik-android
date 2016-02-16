package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.stepic.droid.model.Unit;
import org.stepic.droid.store.structure.DbStructureUnit;
import org.stepic.droid.util.DbParseHelper;

import java.util.List;

import javax.inject.Inject;

public class UnitDaoImpl implements IDao<Unit> {

    IDaoHelper mDaoHelper;

    @Inject
    public UnitDaoImpl(IDaoHelper daoHelper) {
        mDaoHelper = daoHelper;
    }

    @Override
    public void insertOrUpdate(Unit unit) {
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
        mDaoHelper.insertOrUpdate(DbStructureUnit.UNITS, values, DbStructureUnit.Column.UNIT_ID, unit.getId() + "");
    }

    @Override
    public boolean isInDb(Unit persistentObject) {
        return mDaoHelper.isInDb(DbStructureUnit.UNITS, DbStructureUnit.Column.UNIT_ID, persistentObject.getId() + "");
    }

    @Override
    public List<Unit> getAll() {
        throw new RuntimeException();
    }

    @Override
    public List<Unit> getAll(String whereColumnName, String whereValue) {
        throw new RuntimeException();
    }

    @Override
    public Unit get(String whereColumnName, String whereValue) {
        throw new RuntimeException();
    }

    @Override
    public Unit parsePersistentObject(Cursor cursor) {
        throw new RuntimeException();
    }


}
