package org.stepic.droid.store.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import org.stepic.droid.model.CalendarSection
import org.stepic.droid.store.structure.DbStructureCalendarSection

class CalendarSectionDaoImpl(writableDatabase: SQLiteDatabase) : DaoBase<CalendarSection>(writableDatabase) {

    public override fun getDbName(): String {
        return DbStructureCalendarSection.CALENDAR_SECTION
    }

    public override fun getDefaultPrimaryColumn(): String {
        return DbStructureCalendarSection.Column.SECTION_ID
    }

    public override fun getDefaultPrimaryValue(persistentObject: CalendarSection): String {
        return persistentObject.id.toString()
    }

    public override fun getContentValues(persistentObject: CalendarSection): ContentValues? {
        val contentValues = ContentValues()
        contentValues.put(DbStructureCalendarSection.Column.SECTION_ID, persistentObject.id)
        contentValues.put(DbStructureCalendarSection.Column.EVENT_ID, persistentObject.eventId)
        contentValues.put(DbStructureCalendarSection.Column.LAST_STORED_DEADLINE, persistentObject.mostLastDeadline)
        return contentValues
    }

    public override fun parsePersistentObject(cursor: Cursor): CalendarSection? {
        val indexSection = cursor.getColumnIndex(DbStructureCalendarSection.Column.SECTION_ID)
        val indexEvent = cursor.getColumnIndex(DbStructureCalendarSection.Column.EVENT_ID)
        val indexDeadline = cursor.getColumnIndex(DbStructureCalendarSection.Column.LAST_STORED_DEADLINE)

        val calendarSection = CalendarSection(
                id = cursor.getLong(indexSection),
                eventId = cursor.getLong(indexEvent),
                mostLastDeadline = cursor.getString(indexDeadline))

        return calendarSection
    }
}
