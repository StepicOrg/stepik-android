package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter

import org.stepik.android.model.Actions
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Section
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureSections
import org.stepic.droid.util.DbParseHelper

import javax.inject.Inject

class SectionDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val dateAdapter: UTCDateAdapter
) : DaoBase<Section>(databaseOperations) {
    public override fun getDbName(): String =
        DbStructureSections.SECTIONS

    public override fun getDefaultPrimaryColumn(): String =
        DbStructureSections.Column.SECTION_ID

    public override fun getDefaultPrimaryValue(persistentObject: Section): String =
        persistentObject.id.toString()

    public override fun parsePersistentObject(cursor: Cursor): Section {
        val columnIndexId = cursor.getColumnIndex(DbStructureSections.Column.SECTION_ID)
        val columnIndexTitle = cursor.getColumnIndex(DbStructureSections.Column.TITLE)
        val columnIndexSlug = cursor.getColumnIndex(DbStructureSections.Column.SLUG)
        val columnIndexIsActive = cursor.getColumnIndex(DbStructureSections.Column.IS_ACTIVE)
        val columnIndexBeginDate = cursor.getColumnIndex(DbStructureSections.Column.BEGIN_DATE)
        val columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureSections.Column.SOFT_DEADLINE)
        val columnIndexHardDeadline = cursor.getColumnIndex(DbStructureSections.Column.HARD_DEADLINE)
        val columnIndexCourseId = cursor.getColumnIndex(DbStructureSections.Column.COURSE)
        val columnIndexPosition = cursor.getColumnIndex(DbStructureSections.Column.POSITION)
        val columnIndexUnits = cursor.getColumnIndex(DbStructureSections.Column.UNITS)
        val indexTestSection = cursor.getColumnIndex(DbStructureSections.Column.TEST_SECTION)
        val indexDiscountingPolicy = cursor.getColumnIndex(DbStructureSections.Column.DISCOUNTING_POLICY)
        val indexIsExam = cursor.getColumnIndex(DbStructureSections.Column.IS_EXAM)
        val indexProgress = cursor.getColumnIndex(DbStructureSections.Column.PROGRESS)
        val indexIsRequirementSatisfied = cursor.getColumnIndex(DbStructureSections.Column.IS_REQUIREMENT_SATISFIED)
        val indexRequiredSection = cursor.getColumnIndex(DbStructureSections.Column.REQUIRED_SECTION)
        val indexRequiredPercent = cursor.getColumnIndex(DbStructureSections.Column.REQUIRED_PERCENT)

        val actions = Actions(testSection = cursor.getString(indexTestSection))
        val units = DbParseHelper.parseStringToLongList(cursor.getString(columnIndexUnits)) ?: emptyList()
        val typeId = cursor.getInt(indexDiscountingPolicy)
        val discountingPolicyType = DiscountingPolicyType.values().getOrNull(typeId)

        return Section(
            id = cursor.getLong(columnIndexId),
            title = cursor.getString(columnIndexTitle),
            slug = cursor.getString(columnIndexSlug),
            isActive = cursor.getInt(columnIndexIsActive) > 0,
            beginDate = dateAdapter.stringToDate(cursor.getString(columnIndexBeginDate)),
            softDeadline = dateAdapter.stringToDate(cursor.getString(columnIndexSoftDeadline)),
            hardDeadline = dateAdapter.stringToDate(cursor.getString(columnIndexHardDeadline)),
            course = cursor.getLong(columnIndexCourseId),
            position = cursor.getInt(columnIndexPosition),
            units = units,
            discountingPolicy = discountingPolicyType,
            progress = cursor.getString(indexProgress),

            actions = actions,

            isExam = cursor.getInt(indexIsExam) > 0,
            isRequirementSatisfied = cursor.getInt(indexIsRequirementSatisfied) > 0,
            requiredSection = cursor.getLong(indexRequiredSection),
            requiredPercent = cursor.getInt(indexRequiredPercent)
        )
    }

    public override fun getContentValues(section: Section): ContentValues {
        val values = ContentValues()

        values.put(DbStructureSections.Column.SECTION_ID, section.id)
        values.put(DbStructureSections.Column.TITLE, section.title)
        values.put(DbStructureSections.Column.SLUG, section.slug)
        values.put(DbStructureSections.Column.IS_ACTIVE, section.isActive)
        values.put(DbStructureSections.Column.BEGIN_DATE, dateAdapter.dateToString(section.beginDate))
        values.put(DbStructureSections.Column.SOFT_DEADLINE, dateAdapter.dateToString(section.softDeadline))
        values.put(DbStructureSections.Column.HARD_DEADLINE, dateAdapter.dateToString(section.hardDeadline))
        values.put(DbStructureSections.Column.COURSE, section.course)
        values.put(DbStructureSections.Column.POSITION, section.position)
        values.put(DbStructureSections.Column.IS_EXAM, section.isExam)
        values.put(DbStructureSections.Column.UNITS, DbParseHelper.parseLongListToString(section.units))
        values.put(DbStructureSections.Column.PROGRESS, section.progress)

        values.put(DbStructureSections.Column.DISCOUNTING_POLICY, section.discountingPolicy?.ordinal ?: -1)

        values.put(DbStructureSections.Column.TEST_SECTION, section.actions?.testSection)

        values.put(DbStructureSections.Column.IS_REQUIREMENT_SATISFIED, section.isRequirementSatisfied)
        values.put(DbStructureSections.Column.REQUIRED_SECTION, section.requiredSection)
        values.put(DbStructureSections.Column.REQUIRED_PERCENT, section.requiredPercent)

        return values
    }
}
