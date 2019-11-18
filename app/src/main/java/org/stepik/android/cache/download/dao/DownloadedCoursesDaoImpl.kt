package org.stepik.android.cache.download.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentState
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepik.android.cache.section.structure.DbStructureSection
import org.stepik.android.cache.step.structure.DbStructureStep
import org.stepik.android.cache.unit.structure.DbStructureUnit
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
        val sql = "SELECT ${DbStructureSection.Columns.COURSE} FROM ${DbStructureSection.TABLE_NAME} WHERE ${DbStructureSection.Columns.ID} " +
                "IN (SELECT ${DbStructureUnit.Columns.SECTION} FROM ${DbStructureUnit.TABLE_NAME} WHERE ${DbStructureUnit.Columns.LESSON} " +
                "IN (SELECT ${DbStructureStep.Column.LESSON_ID} FROM ${DbStructureStep.TABLE_NAME} WHERE ${DbStructureStep.Column.ID} " +
                "IN (SELECT ${DBStructurePersistentState.Columns.ID} FROM ${DBStructurePersistentState.TABLE_NAME} WHERE " +
                "${DBStructurePersistentState.Columns.TYPE} = \'${PersistentState.Type.STEP}\' AND ${DBStructurePersistentState.Columns.STATE} = \'${PersistentState.State.CACHED}\')))"
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