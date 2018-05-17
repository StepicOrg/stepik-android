package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.deadlines.DeadlineFlatItem
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureDeadlines
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PersonalDeadlinesDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<DeadlineFlatItem>(databaseOperations), PersonalDeadlinesDao {
    override fun getDbName() = DbStructureDeadlines.DEADLINES

    override fun getDefaultPrimaryColumn(): String = DbStructureDeadlines.Columns.RECORD_ID

    override fun getDefaultPrimaryValue(persistentObject: DeadlineFlatItem): String =
            persistentObject.recordId.toString()

    override fun getContentValues(persistentObject: DeadlineFlatItem) = ContentValues().apply {
        put(DbStructureDeadlines.Columns.RECORD_ID, persistentObject.recordId)
        put(DbStructureDeadlines.Columns.COURSE_ID, persistentObject.courseId)
        put(DbStructureDeadlines.Columns.SECTION_ID, persistentObject.sectionId)
        put(DbStructureDeadlines.Columns.DEADLINE, persistentObject.deadline.time)
    }

    override fun parsePersistentObject(cursor: Cursor) = DeadlineFlatItem(
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.RECORD_ID)),
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.COURSE_ID)),
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.SECTION_ID)),
            Date(cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.DEADLINE)))
    )

    override fun getClosestDeadlineDate(): Date? =
            rawQuery("SELECT ${DbStructureDeadlines.Columns.DEADLINE} FROM $dbName ORDER BY ${DbStructureDeadlines.Columns.DEADLINE} DECS LIMIT 1", null) {
                return@rawQuery if (it.moveToFirst()) {
                    Date(it.getLong(it.getColumnIndex(DbStructureDeadlines.Columns.DEADLINE)))
                } else {
                    null
                }
            }

    override fun getDeadlinesForDate(date: Date, gap: Long): List<DeadlineFlatItem> =
            rawQuery("SELECT * FROM $dbName WHERE ${DbStructureDeadlines.Columns.DEADLINE} BETWEEN ? AND ?", arrayOf("${date.time - gap}", "${date.time + gap}")) {
                val res = ArrayList<DeadlineFlatItem>()

                if (it.moveToFirst()) {
                    do {
                        res.add(parsePersistentObject(it))
                    } while (it.moveToNext())
                }

                return@rawQuery res
            }
}