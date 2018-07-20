package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.model.CalendarSection
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureCalendarSection
import javax.inject.Inject

class CalendarSectionDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val dateAdapter: UTCDateAdapter
) : DaoBase<CalendarSection>(databaseOperations) {

    public override fun getDbName(): String = DbStructureCalendarSection.CALENDAR_SECTION

    public override fun getDefaultPrimaryColumn(): String =
            DbStructureCalendarSection.Column.SECTION_ID

    public override fun getDefaultPrimaryValue(persistentObject: CalendarSection): String =
            persistentObject.id.toString()

    public override fun getContentValues(persistentObject: CalendarSection): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureCalendarSection.Column.SECTION_ID, persistentObject.id)
        contentValues.put(DbStructureCalendarSection.Column.EVENT_ID_HARD, persistentObject.eventIdHardDeadline)
        contentValues.put(DbStructureCalendarSection.Column.HARD_DEADLINE, dateAdapter.dateToString(persistentObject.hardDeadline))
        contentValues.put(DbStructureCalendarSection.Column.SOFT_DEADLINE, dateAdapter.dateToString(persistentObject.softDeadline))
        contentValues.put(DbStructureCalendarSection.Column.EVENT_ID_SOFT, persistentObject.eventIdSoftDeadline)
        return contentValues
    }

    public override fun parsePersistentObject(cursor: Cursor): CalendarSection {
        val indexSection = cursor.getColumnIndex(DbStructureCalendarSection.Column.SECTION_ID)
        val indexHardDeadlineEvent = cursor.getColumnIndex(DbStructureCalendarSection.Column.EVENT_ID_HARD)
        val indexSoftDeadlineEvent = cursor.getColumnIndex(DbStructureCalendarSection.Column.EVENT_ID_SOFT)
        val indexDeadline = cursor.getColumnIndex(DbStructureCalendarSection.Column.HARD_DEADLINE)
        val indexSoftDeadline = cursor.getColumnIndex(DbStructureCalendarSection.Column.SOFT_DEADLINE)

        var eventIdHardDeadline: Long? = cursor.getLong(indexHardDeadlineEvent)
        if (eventIdHardDeadline == 0L) eventIdHardDeadline = null

        var eventIdSoftDeadline: Long? = cursor.getLong(indexSoftDeadlineEvent)
        if (eventIdSoftDeadline == 0L) eventIdSoftDeadline = null

        return CalendarSection(
                id = cursor.getLong(indexSection),
                eventIdHardDeadline = eventIdHardDeadline,
                eventIdSoftDeadline = eventIdSoftDeadline,
                hardDeadline = dateAdapter.stringToDate(cursor.getString(indexDeadline)),
                softDeadline = dateAdapter.stringToDate(cursor.getString(indexSoftDeadline))
        )
    }
}
