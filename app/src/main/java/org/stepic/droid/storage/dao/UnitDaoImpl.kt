package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureUnit
import org.stepic.droid.util.DbParseHelper
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val dateAdapter: UTCDateAdapter
) : DaoBase<Unit>(databaseOperations) {
    public override fun getDbName() = DbStructureUnit.UNITS

    public override fun getDefaultPrimaryColumn() = DbStructureUnit.Column.UNIT_ID

    public override fun getDefaultPrimaryValue(persistentObject: Unit): String =
        persistentObject.id.toString()

    public override fun parsePersistentObject(cursor: Cursor): Unit {
        val columnIndexUnitId = cursor.getColumnIndex(DbStructureUnit.Column.UNIT_ID)
        val columnIndexSection = cursor.getColumnIndex(DbStructureUnit.Column.SECTION)
        val columnIndexLesson = cursor.getColumnIndex(DbStructureUnit.Column.LESSON)
        val columnIndexAssignments = cursor.getColumnIndex(DbStructureUnit.Column.ASSIGNMENTS)
        val columnIndexPosition = cursor.getColumnIndex(DbStructureUnit.Column.POSITION)
        val columnIndexProgress = cursor.getColumnIndex(DbStructureUnit.Column.PROGRESS)
        val columnIndexBeginDate = cursor.getColumnIndex(DbStructureUnit.Column.BEGIN_DATE)
        val columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureUnit.Column.SOFT_DEADLINE)
        val columnIndexHardDeadline = cursor.getColumnIndex(DbStructureUnit.Column.HARD_DEADLINE)
        val columnIndexIsActive = cursor.getColumnIndex(DbStructureUnit.Column.IS_ACTIVE)

        return Unit(
                id = cursor.getLong(columnIndexUnitId),
                section = cursor.getLong(columnIndexSection),
                lesson = cursor.getLong(columnIndexLesson),
                progress = cursor.getString(columnIndexProgress),
                assignments = DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexAssignments)),
                beginDate = dateAdapter.stringToDate(cursor.getString(columnIndexBeginDate)),
                softDeadline = dateAdapter.stringToDate(cursor.getString(columnIndexSoftDeadline)),
                hardDeadline = dateAdapter.stringToDate(cursor.getString(columnIndexHardDeadline)),
                position = cursor.getInt(columnIndexPosition),
                isActive = cursor.getInt(columnIndexIsActive) > 0
        )
    }

    public override fun getContentValues(unit: Unit): ContentValues {
        val values = ContentValues()
        values.put(DbStructureUnit.Column.UNIT_ID, unit.id)
        values.put(DbStructureUnit.Column.SECTION, unit.section)
        values.put(DbStructureUnit.Column.LESSON, unit.lesson)
        values.put(DbStructureUnit.Column.ASSIGNMENTS, DbParseHelper.parseLongArrayToString(unit.assignments))
        values.put(DbStructureUnit.Column.POSITION, unit.position)
        values.put(DbStructureUnit.Column.PROGRESS, unit.progress)
        values.put(DbStructureUnit.Column.BEGIN_DATE, dateAdapter.dateToString(unit.beginDate))
        values.put(DbStructureUnit.Column.END_DATE, dateAdapter.dateToString(unit.endDate))
        values.put(DbStructureUnit.Column.SOFT_DEADLINE, dateAdapter.dateToString(unit.softDeadline))
        values.put(DbStructureUnit.Column.HARD_DEADLINE, dateAdapter.dateToString(unit.hardDeadline))
        values.put(DbStructureUnit.Column.GRADING_POLICY, unit.gradingPolicy)
        values.put(DbStructureUnit.Column.BEGIN_DATE_SOURCE, unit.beginDateSource)
        values.put(DbStructureUnit.Column.END_DATE_SOURCE, unit.endDateSource)
        values.put(DbStructureUnit.Column.SOFT_DEADLINE_SOURCE, unit.softDeadlineSource)
        values.put(DbStructureUnit.Column.HARD_DEADLINE_SOURCE, unit.hardDeadlineSource)
        values.put(DbStructureUnit.Column.GRADING_POLICY_SOURCE, unit.gradingPolicySource)
        values.put(DbStructureUnit.Column.IS_ACTIVE, unit.isActive)
        values.put(DbStructureUnit.Column.CREATE_DATE, dateAdapter.dateToString(unit.createDate))
        values.put(DbStructureUnit.Column.UPDATE_DATE, dateAdapter.dateToString(unit.updateDate))
        return values
    }
}