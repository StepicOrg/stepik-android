package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.stepik.android.model.actions.Actions;
import org.stepic.droid.model.DiscountingPolicyType;
import org.stepic.droid.model.Section;
import org.stepic.droid.storage.operations.DatabaseOperations;
import org.stepic.droid.storage.structure.DbStructureSections;
import org.stepic.droid.util.DbParseHelper;

import javax.inject.Inject;

public class SectionDaoImpl extends DaoBase<Section> {

    @Inject
    public SectionDaoImpl(DatabaseOperations databaseOperations) {
        super(databaseOperations);
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
        int indexTestSection = cursor.getColumnIndex(DbStructureSections.Column.TEST_SECTION);
        int indexDiscountingPolicy = cursor.getColumnIndex(DbStructureSections.Column.DISCOUNTING_POLICY);
        int indexIsExam = cursor.getColumnIndex(DbStructureSections.Column.IS_EXAM);
        int indexProgress = cursor.getColumnIndex(DbStructureSections.Column.PROGRESS);
        int indexIsRequirementSatisfied = cursor.getColumnIndex(DbStructureSections.Column.IS_REQUIREMENT_SATISFIED);
        int indexRequiredSection = cursor.getColumnIndex(DbStructureSections.Column.REQUIRED_SECTION);
        int indexRequiredPercent = cursor.getColumnIndex(DbStructureSections.Column.REQUIRED_PERCENT);

        section.setId(cursor.getLong(columnIndexId));
        section.setTitle(cursor.getString(columnIndexTitle));
        section.setSlug(cursor.getString(columnIndexSlug));
        section.setActive(cursor.getInt(columnIndexIsActive) > 0);
        section.setBeginDate(cursor.getString(columnIndexBeginDate));
        section.setSoftDeadline(cursor.getString(columnIndexSoftDeadline));
        section.setHardDeadline(cursor.getString(columnIndexHardDeadline));
        section.setCourse(cursor.getLong(columnIndexCourseId));
        section.setPosition(cursor.getInt(columnIndexPosition));
        section.setCached(cursor.getInt(indexIsCached) > 0);
        section.setLoading(cursor.getInt(indexIsLoading) > 0);
        long[] units = DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexUnits));
        section.setUnits(units == null ? new long[0] : units);
        int typeId = cursor.getInt(indexDiscountingPolicy);
        DiscountingPolicyType discountingPolicyType = getDiscountingPolicyType(typeId);
        section.setDiscountingPolicy(discountingPolicyType);
        section.setProgress(cursor.getString(indexProgress));

        String canTestSection = cursor.getString(indexTestSection);
        Actions actions = new Actions(false, false, canTestSection);
        section.setActions(actions);

        section.setExam(cursor.getInt(indexIsExam) > 0);
        section.setRequirementSatisfied(cursor.getInt(indexIsRequirementSatisfied) > 0);
        section.setRequiredSection(cursor.getLong(indexRequiredSection));
        section.setRequiredPercent(cursor.getInt(indexRequiredPercent));

        return section;
    }

    private DiscountingPolicyType getDiscountingPolicyType(int typeId) {
        DiscountingPolicyType[] localValues = DiscountingPolicyType.values();
        if (typeId >= 0 && typeId < localValues.length) {
            return localValues[typeId];
        } else {
            return null;
        }
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
        values.put(DbStructureSections.Column.IS_ACTIVE, section.isActive());
        values.put(DbStructureSections.Column.BEGIN_DATE, section.getBeginDate());
        values.put(DbStructureSections.Column.SOFT_DEADLINE, section.getSoftDeadline());
        values.put(DbStructureSections.Column.HARD_DEADLINE, section.getHardDeadline());
        values.put(DbStructureSections.Column.COURSE, section.getCourse());
        values.put(DbStructureSections.Column.POSITION, section.getPosition());
        values.put(DbStructureSections.Column.IS_EXAM, section.isExam());
        values.put(DbStructureSections.Column.UNITS, DbParseHelper.parseLongArrayToString(section.getUnits()));
        values.put(DbStructureSections.Column.PROGRESS, section.getProgress());
        if (section.getDiscountingPolicy() != null) {
            values.put(DbStructureSections.Column.DISCOUNTING_POLICY, section.getDiscountingPolicy().ordinal());
        } else {
            values.put(DbStructureSections.Column.DISCOUNTING_POLICY, -1);
        }

        if (section.getActions() != null && section.getActions().getTestSection() != null) {
            values.put(DbStructureSections.Column.TEST_SECTION, section.getActions().getTestSection());
        }
        values.put(DbStructureSections.Column.IS_REQUIREMENT_SATISFIED, section.isRequirementSatisfied());
        values.put(DbStructureSections.Column.REQUIRED_SECTION, section.getRequiredSection());
        values.put(DbStructureSections.Column.REQUIRED_PERCENT, section.getRequiredPercent());

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
