package org.stepik.android.cache.download.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import javax.inject.Inject

class DownloadedCoursesDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<Long>(databaseOperations), DownloadedCoursesDao {
    object Columns {
        const val COURSE = "course"
    }
    override fun getDbName(): String = ""

    override fun getDefaultPrimaryColumn(): String = ""

    override fun getDefaultPrimaryValue(persistentObject: Long?): String = ""

    override fun getContentValues(persistentObject: Long): ContentValues =
        ContentValues()

    override fun parsePersistentObject(cursor: Cursor): Long =
        cursor.getLong(Columns.COURSE)

    override fun getCourseIds(): List<Long> {
        val sql = "SELECT course FROM section WHERE id IN (SELECT section FROM unit WHERE lesson IN (SELECT lesson_id FROM step WHERE id IN (SELECT id FROM persistent_state WHERE type = \'STEP\' AND state = \'CACHED\')))"
        return rawQuery(sql, null) {
            val res = ArrayList<Long>()

            if (it.moveToFirst()) {
                do {
                    res.add(parsePersistentObject(it))
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
    }
}