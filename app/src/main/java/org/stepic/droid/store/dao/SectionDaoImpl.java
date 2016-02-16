package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Section;
import org.stepic.droid.store.structure.DbStructureSections;
import org.stepic.droid.util.DbParseHelper;

public class SectionDaoImpl extends DaoBase<Section> {

    public SectionDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
    }

    @Override
    public Section parsePersistentObject(Cursor cursor) {
        Section section = new Section();

        int columnIndexId = cursor.getColumnIndex(DbStructureSections.Column.SECTION_ID);
        int columnIndexTitle = cursor.getColumnIndex(DbStructureSections.Column.TITLE);
        int columnIndexSlug = cursor.getColumnIndex(DbStructureSections.Column.SLUG);
        int columnIndexIsActive = cursor.getColumnIndex(DbStructureSections.Column.IS_ACTIVE);
        int columnIndexBeginDate = cursor.getColumnIndex(DbStructureSections.Column.BEGIN_DATE);
        int columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureSections.Column.SOFT_DEADLINE);
        int columnIndexHardDeadline = cursor.getColumnIndex(DbStructureSections.Column.HARD_DEADLINE);
        int columnIndexCourseId = cursor.getColumnIndex(DbStructureSections.Column.COURSE);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureSections.Column.POSITION);
        int columnIndexUnits = cursor.getColumnIndex(DbStructureSections.Column.UNITS);
        int indexIsCached = cursor.getColumnIndex(DbStructureSections.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DbStructureSections.Column.IS_LOADING);

        section.setId(cursor.getLong(columnIndexId));
        section.setTitle(cursor.getString(columnIndexTitle));
        section.setSlug(cursor.getString(columnIndexSlug));
        section.setIs_active(cursor.getInt(columnIndexIsActive) > 0);
        section.setBegin_date(cursor.getString(columnIndexBeginDate));
        section.setSoft_deadline(cursor.getString(columnIndexSoftDeadline));
        section.setHard_deadline(cursor.getString(columnIndexHardDeadline));
        section.setCourse(cursor.getLong(columnIndexCourseId));
        section.setPosition(cursor.getInt(columnIndexPosition));
        section.setIs_cached(cursor.getInt(indexIsCached) > 0);
        section.setIs_loading(cursor.getInt(indexIsLoading) > 0);
        section.setUnits(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexUnits)));

        return section;
    }

    @Override
    public String getDbName() {
        return DbStructureSections.SECTIONS;
    }

    @Override
    public ContentValues getContentValues(Section section) {
        ContentValues values = new ContentValues();

        values.put(DbStructureSections.Column.SECTION_ID, section.getId());
        values.put(DbStructureSections.Column.TITLE, section.getTitle());
        values.put(DbStructureSections.Column.SLUG, section.getSlug());
        values.put(DbStructureSections.Column.IS_ACTIVE, section.is_active());
        values.put(DbStructureSections.Column.BEGIN_DATE, section.getBegin_date());
        values.put(DbStructureSections.Column.SOFT_DEADLINE, section.getSoft_deadline());
        values.put(DbStructureSections.Column.HARD_DEADLINE, section.getHard_deadline());
        values.put(DbStructureSections.Column.COURSE, section.getCourse());
        values.put(DbStructureSections.Column.POSITION, section.getPosition());
        values.put(DbStructureSections.Column.UNITS, DbParseHelper.parseLongArrayToString(section.getUnits()));

        return values;
    }


    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureSections.Column.SECTION_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Section persistentObject) {
        return persistentObject.getId() + "";
    }
}
