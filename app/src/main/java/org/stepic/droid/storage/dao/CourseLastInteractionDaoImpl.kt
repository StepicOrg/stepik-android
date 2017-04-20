package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.model.CourseLastInteraction
import org.stepic.droid.storage.structure.DbStructureCourseLastInteraction
import javax.inject.Inject

class CourseLastInteractionDaoImpl @Inject constructor(writableDatabase: SQLiteDatabase) : DaoBase<CourseLastInteraction>(writableDatabase) {
    override fun getDefaultPrimaryColumn()
            = DbStructureCourseLastInteraction.Column.COURSE_ID

    override fun getDefaultPrimaryValue(persistentObject: CourseLastInteraction) = persistentObject.courseId.toString()

    override fun getContentValues(persistentObject: CourseLastInteraction): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureCourseLastInteraction.Column.COURSE_ID, persistentObject.courseId)
        contentValues.put(DbStructureCourseLastInteraction.Column.TIMESTAMP, persistentObject.timestamp)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): CourseLastInteraction {
        val indexCourseId = cursor.getColumnIndex(DbStructureCourseLastInteraction.Column.COURSE_ID)
        val indexTimestamp = cursor.getColumnIndex(DbStructureCourseLastInteraction.Column.TIMESTAMP)

        val courseId = cursor.getLong(indexCourseId)
        val timestamp = cursor.getLong(indexTimestamp)

        return CourseLastInteraction(courseId = courseId,
                timestamp = timestamp)
    }

    override fun getDbName() = DbStructureCourseLastInteraction.COURSE_LAST_INTERACTION
}
