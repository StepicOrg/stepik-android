package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import org.stepic.droid.model.CalendarSection
import org.stepic.droid.storage.structure.DbStructureCalendarSection
import javax.inject.Inject

class CalendarSectionDaoImpl @Inject constructor(writableDatabase: SQLiteDatabase) : DaoBase<CalendarSection>(writableDatabase) {

    public override fun getDbName(): String {
        return DbStructureCalendarSection.CALENDAR_SECTION
    }

    public override fun getDefaultPrimaryColumn(): String {
        return DbStructureCalendarSection.Column.SECTION_ID
    }

    public override fun getDefaultPrimaryValue(persistentObject: CalendarSection): String {
        return persistentObject.id.toString()
    }

    public override fun getContentValues(persistentObject: CalendarSection): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureCalendarSection.Column.SECTION_ID, persistentObject.id)
        contentValues.put(DbStructureCalendarSection.Column.EVENT_ID_HARD, persistentObject.eventIdHardDeadline)
        contentValues.put(DbStructureCalendarSection.Column.HARD_DEADLINE, persistentObject.hardDeadline)
        contentValues.put(DbStructureCalendarSection.Column.SOFT_DEADLINE, persistentObject.softDeadline)
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

        val calendarSection = CalendarSection(
                id = cursor.getLong(indexSection),
                eventIdHardDeadline = eventIdHardDeadline,
                eventIdSoftDeadline = eventIdSoftDeadline,
                hardDeadline = cursor.getString(indexDeadline),
                softDeadline = cursor.getString(indexSoftDeadline)
        )

        return calendarSection
    }
}
