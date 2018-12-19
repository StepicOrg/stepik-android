package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor

import org.stepik.android.model.Actions
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Section
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureSections
import org.stepic.droid.util.*
import org.stepik.android.cache.section.structure.DbStructureSection

import javax.inject.Inject

class SectionDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Section>(databaseOperations) {
    public override fun getDbName(): String =
        DbStructureSections.SECTIONS

    public override fun getDefaultPrimaryColumn(): String =
        DbStructureSections.Column.SECTION_ID

    public override fun getDefaultPrimaryValue(persistentObject: Section): String =
        persistentObject.id.toString()

    public override fun parsePersistentObject(cursor: Cursor): Section =
        Section(
            id = cursor.getLong(DbStructureSection.Columns.ID),
            course = cursor.getLong(DbStructureSection.Columns.COURSE),
            units = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureSection.Columns.UNITS)) ?: emptyList(),
            position = cursor.getInt(DbStructureSection.Columns.POSITION),
            progress = cursor.getString(DbStructureSection.Columns.PROGRESS),
            title = cursor.getString(DbStructureSection.Columns.TITLE),
            slug = cursor.getString(DbStructureSection.Columns.SLUG),
            beginDate = cursor.getDate(DbStructureSection.Columns.BEGIN_DATE),
            endDate = cursor.getDate(DbStructureSection.Columns.END_DATE),
            softDeadline = cursor.getDate(DbStructureSection.Columns.SOFT_DEADLINE),
            hardDeadline = cursor.getDate(DbStructureSection.Columns.HARD_DEADLINE),
            createDate = cursor.getDate(DbStructureSection.Columns.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureSection.Columns.UPDATE_DATE),
            gradingPolicy = cursor.getString(DbStructureSection.Columns.GRADING_POLICY),
            isActive = cursor.getBoolean(DbStructureSection.Columns.IS_ACTIVE),
            actions = Actions(testSection = cursor.getString(DbStructureSection.Columns.ACTIONS_TEST_SECTION)),
            isExam = cursor.getBoolean(DbStructureSection.Columns.IS_EXAM),
            discountingPolicy = DiscountingPolicyType.values().getOrNull(cursor.getInt(DbStructureSection.Columns.DISCOUNTING_POLICY)),
            isRequirementSatisfied = cursor.getBoolean(DbStructureSection.Columns.IS_REQUIREMENT_SATISFIED),
            requiredSection = cursor.getLong(DbStructureSection.Columns.REQUIRED_SECTION),
            requiredPercent = cursor.getInt(DbStructureSection.Columns.REQUIRED_PERCENT)
        )

    public override fun getContentValues(section: Section): ContentValues {
        val values = ContentValues()

        values.put(DbStructureSection.Columns.ID, section.id)
        values.put(DbStructureSection.Columns.COURSE, section.course)
        values.put(DbStructureSection.Columns.UNITS, DbParseHelper.parseLongListToString(section.units))
        values.put(DbStructureSection.Columns.POSITION, section.position)
        values.put(DbStructureSection.Columns.PROGRESS, section.progress)
        values.put(DbStructureSection.Columns.TITLE, section.title)
        values.put(DbStructureSection.Columns.SLUG, section.slug)
        values.put(DbStructureSection.Columns.BEGIN_DATE, section.beginDate?.time ?: -1)
        values.put(DbStructureSection.Columns.END_DATE, section.endDate?.time ?: -1)
        values.put(DbStructureSection.Columns.SOFT_DEADLINE, section.softDeadline?.time ?: -1)
        values.put(DbStructureSection.Columns.HARD_DEADLINE, section.hardDeadline?.time ?: -1)
        values.put(DbStructureSection.Columns.CREATE_DATE, section.createDate?.time ?: -1)
        values.put(DbStructureSection.Columns.UPDATE_DATE, section.updateDate?.time ?: -1)
        values.put(DbStructureSection.Columns.GRADING_POLICY, section.gradingPolicy)
        values.put(DbStructureSection.Columns.IS_ACTIVE, section.isActive)
        values.put(DbStructureSection.Columns.ACTIONS_TEST_SECTION, section.actions?.testSection)
        values.put(DbStructureSection.Columns.IS_EXAM, section.isExam)
        values.put(DbStructureSection.Columns.DISCOUNTING_POLICY, section.discountingPolicy?.ordinal ?: -1)
        values.put(DbStructureSection.Columns.IS_REQUIREMENT_SATISFIED, section.isRequirementSatisfied)
        values.put(DbStructureSection.Columns.REQUIRED_SECTION, section.requiredSection)
        values.put(DbStructureSection.Columns.REQUIRED_PERCENT, section.requiredPercent)

        return values
    }
}
