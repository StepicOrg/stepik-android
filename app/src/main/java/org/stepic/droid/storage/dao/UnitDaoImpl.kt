package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.unit.structure.DbStructureUnit
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Unit>(databaseOperations) {
    public override fun getDbName() = DbStructureUnit.TABLE_NAME

    public override fun getDefaultPrimaryColumn() = DbStructureUnit.Columns.ID

    public override fun getDefaultPrimaryValue(persistentObject: Unit): String =
        persistentObject.id.toString()

    public override fun parsePersistentObject(cursor: Cursor): Unit =
        Unit(
            id = cursor.getLong(DbStructureUnit.Columns.ID),
            section = cursor.getLong(DbStructureUnit.Columns.SECTION),
            lesson = cursor.getLong(DbStructureUnit.Columns.LESSON),
            assignments = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureUnit.Columns.ASSIGNMENTS)),
            position = cursor.getInt(DbStructureUnit.Columns.POSITION),
            progress = cursor.getString(DbStructureUnit.Columns.PROGRESS),
            beginDate = cursor.getDate(DbStructureUnit.Columns.BEGIN_DATE),
            endDate = cursor.getDate(DbStructureUnit.Columns.END_DATE),
            softDeadline = cursor.getDate(DbStructureUnit.Columns.SOFT_DEADLINE),
            hardDeadline = cursor.getDate(DbStructureUnit.Columns.HARD_DEADLINE),
            gradingPolicy = cursor.getString(DbStructureUnit.Columns.GRADING_POLICY),
            beginDateSource = cursor.getString(DbStructureUnit.Columns.BEGIN_DATE_SOURCE),
            endDateSource = cursor.getString(DbStructureUnit.Columns.END_DATE_SOURCE),
            softDeadlineSource = cursor.getString(DbStructureUnit.Columns.SOFT_DEADLINE_SOURCE),
            hardDeadlineSource = cursor.getString(DbStructureUnit.Columns.HARD_DEADLINE_SOURCE),
            gradingPolicySource = cursor.getString(DbStructureUnit.Columns.GRADING_POLICY_SOURCE),
            isActive = cursor.getBoolean(DbStructureUnit.Columns.IS_ACTIVE),
            createDate = cursor.getDate(DbStructureUnit.Columns.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureUnit.Columns.UPDATE_DATE)
        )

    public override fun getContentValues(unit: Unit): ContentValues {
        val values = ContentValues()
        values.put(DbStructureUnit.Columns.ID, unit.id)
        values.put(DbStructureUnit.Columns.SECTION, unit.section)
        values.put(DbStructureUnit.Columns.LESSON, unit.lesson)
        values.put(DbStructureUnit.Columns.ASSIGNMENTS, DbParseHelper.parseLongListToString(unit.assignments))
        values.put(DbStructureUnit.Columns.POSITION, unit.position)
        values.put(DbStructureUnit.Columns.PROGRESS, unit.progress)
        values.put(DbStructureUnit.Columns.BEGIN_DATE, unit.beginDate?.time ?: -1)
        values.put(DbStructureUnit.Columns.END_DATE, unit.endDate?.time ?: -1)
        values.put(DbStructureUnit.Columns.SOFT_DEADLINE, unit.softDeadline?.time ?: -1)
        values.put(DbStructureUnit.Columns.HARD_DEADLINE, unit.hardDeadline?.time ?: -1)
        values.put(DbStructureUnit.Columns.GRADING_POLICY, unit.gradingPolicy)
        values.put(DbStructureUnit.Columns.BEGIN_DATE_SOURCE, unit.beginDateSource)
        values.put(DbStructureUnit.Columns.END_DATE_SOURCE, unit.endDateSource)
        values.put(DbStructureUnit.Columns.SOFT_DEADLINE_SOURCE, unit.softDeadlineSource)
        values.put(DbStructureUnit.Columns.HARD_DEADLINE_SOURCE, unit.hardDeadlineSource)
        values.put(DbStructureUnit.Columns.GRADING_POLICY_SOURCE, unit.gradingPolicySource)
        values.put(DbStructureUnit.Columns.IS_ACTIVE, unit.isActive)
        values.put(DbStructureUnit.Columns.CREATE_DATE, unit.createDate?.time ?: -1)
        values.put(DbStructureUnit.Columns.UPDATE_DATE, unit.updateDate?.time ?: -1)
        return values
    }
}