package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import org.stepik.android.model.structure.Progress;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.operations.DatabaseOperations;
import org.stepic.droid.storage.structure.DbStructureProgress;
import org.stepic.droid.storage.structure.DbStructureUnit;
import org.stepic.droid.util.DbParseHelper;

import java.util.List;

import javax.inject.Inject;

public class UnitDaoImpl extends DaoBase<Unit> {

    private final IDao<Progress> progressDao;

    @Inject
    public UnitDaoImpl(DatabaseOperations databaseOperations, IDao<Progress> progressDao) {
        super(databaseOperations);
        this.progressDao = progressDao;
    }

    @Override
    public Unit parsePersistentObject(Cursor cursor) {
        Unit unit = new Unit();

        int columnIndexUnitId = cursor.getColumnIndex(DbStructureUnit.Column.UNIT_ID);
        int columnIndexSection = cursor.getColumnIndex(DbStructureUnit.Column.SECTION);
        int columnIndexLesson = cursor.getColumnIndex(DbStructureUnit.Column.LESSON);
        int columnIndexAssignments = cursor.getColumnIndex(DbStructureUnit.Column.ASSIGNMENTS);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureUnit.Column.POSITION);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureUnit.Column.PROGRESS);
        int columnIndexBeginDate = cursor.getColumnIndex(DbStructureUnit.Column.BEGIN_DATE);
        int columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureUnit.Column.SOFT_DEADLINE);
        int columnIndexHardDeadline = cursor.getColumnIndex(DbStructureUnit.Column.HARD_DEADLINE);
        int columnIndexIsActive = cursor.getColumnIndex(DbStructureUnit.Column.IS_ACTIVE);

        unit.setId(cursor.getLong(columnIndexUnitId));
        unit.setSection(cursor.getLong(columnIndexSection));
        unit.setLesson(cursor.getLong(columnIndexLesson));
        unit.setProgress(cursor.getString(columnIndexProgress));
        unit.setAssignments(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexAssignments)));
        unit.setBegin_date(cursor.getString(columnIndexBeginDate));
        unit.setSoft_deadline(cursor.getString(columnIndexSoftDeadline));
        unit.setHard_deadline(cursor.getString(columnIndexHardDeadline));
        unit.setPosition(cursor.getInt(columnIndexPosition));
        unit.set_active(cursor.getInt(columnIndexIsActive) > 0);

        return unit;
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
        values.put(DbStructureUnit.Column.PROGRESS, unit.getProgressId());
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

    //For inner objects:

    @Nullable
    @Override
    public Unit get(@NonNull String whereColumnName, @NonNull String whereValue) {
        Unit unit = super.get(whereColumnName, whereValue);
        return determinePassed(unit);
    }

    @Override
    protected List<Unit> getAllWithQuery(String query, String[] whereArgs) {
        List<Unit> unitList = super.getAllWithQuery(query, whereArgs);
        for (Unit unitItem : unitList) {
            determinePassed(unitItem);
        }
        return unitList;
    }

    private Unit determinePassed(Unit unit) {
        boolean isPassed = false;
        if (unit != null) {
            String progressId = unit.getProgressId();
            Progress progress = null;
            if (progressId != null) {
                progress = progressDao.get(DbStructureProgress.Column.ID, progressId);
            }
            if (progress != null)
                isPassed = progress.isPassed();
            unit.set_viewed_custom(isPassed);
        }

        return unit;
    }
}
