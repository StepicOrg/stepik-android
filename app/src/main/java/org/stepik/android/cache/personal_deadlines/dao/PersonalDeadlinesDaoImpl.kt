package org.stepik.android.cache.personal_deadlines.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.dateFromSQLDatetime
import org.stepic.droid.util.toSQLDatetime
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlines
import java.util.Date
import javax.inject.Inject

class PersonalDeadlinesDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<DeadlineEntity>(databaseOperations), PersonalDeadlinesDao {
    override fun getDbName(): String =
        DbStructureDeadlines.DEADLINES

    override fun getDefaultPrimaryColumn(): String =
        DbStructureDeadlines.Columns.SECTION_ID

    override fun getDefaultPrimaryValue(persistentObject: DeadlineEntity): String =
        persistentObject.sectionId.toString()

    override fun getContentValues(persistentObject: DeadlineEntity): ContentValues =
        ContentValues().apply {
            put(DbStructureDeadlines.Columns.RECORD_ID, persistentObject.recordId)
            put(DbStructureDeadlines.Columns.COURSE_ID, persistentObject.courseId)
            put(DbStructureDeadlines.Columns.SECTION_ID, persistentObject.sectionId)
            put(DbStructureDeadlines.Columns.DEADLINE, persistentObject.deadline.toSQLDatetime())
        }

    override fun parsePersistentObject(cursor: Cursor): DeadlineEntity =
        DeadlineEntity(
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.RECORD_ID)),
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.COURSE_ID)),
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlines.Columns.SECTION_ID)),
            dateFromSQLDatetime(cursor.getString(cursor.getColumnIndex(DbStructureDeadlines.Columns.DEADLINE)))
        )

    override fun getClosestDeadlineDate(): Date? =
        rawQuery("SELECT * FROM $dbName WHERE ${DbStructureDeadlines.Columns.DEADLINE} > datetime('now') " +
                "ORDER BY ${DbStructureDeadlines.Columns.DEADLINE} LIMIT 1", null) {
            return@rawQuery if (it.moveToFirst()) {
                parsePersistentObject(it).deadline
            } else {
                null
            }
        }

    override fun getDeadlinesBetween(from: Date, to: Date): List<DeadlineEntity> =
        rawQuery("SELECT * FROM $dbName WHERE ${DbStructureDeadlines.Columns.DEADLINE} BETWEEN ? AND ?", arrayOf(from.toSQLDatetime(), to.toSQLDatetime())) {
            val res = ArrayList<DeadlineEntity>()

            if (it.moveToFirst()) {
                do {
                    res.add(parsePersistentObject(it))
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
}