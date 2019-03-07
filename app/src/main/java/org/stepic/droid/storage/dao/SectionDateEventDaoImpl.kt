package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepik.android.cache.course_calendar.structure.DbStructureSectionDateEvent
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import javax.inject.Inject

class SectionDateEventDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<SectionDateEvent>(databaseOperations) {

    override fun getDbName(): String = DbStructureSectionDateEvent.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureSectionDateEvent.Columns.EVENT_ID

    override fun getDefaultPrimaryValue(persistentObject: SectionDateEvent): String =
        persistentObject.eventId.toString()

    override fun getContentValues(persistentObject: SectionDateEvent): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureSectionDateEvent.Columns.EVENT_ID, persistentObject.eventId)
        contentValues.put(DbStructureSectionDateEvent.Columns.SECTION_ID, persistentObject.sectionId)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): SectionDateEvent =
        SectionDateEvent(
            eventId = cursor.getLong(DbStructureSectionDateEvent.Columns.EVENT_ID),
            sectionId = cursor.getLong(DbStructureSectionDateEvent.Columns.SECTION_ID)
        )
}