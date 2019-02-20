package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.course_reviews.structure.DbStructureCourseReview
import org.stepik.android.domain.course_reviews.model.CourseReview
import javax.inject.Inject

class CourseReviewsDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<CourseReview>(databaseOperations) {
    override fun getDbName(): String =
        DbStructureCourseReview.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCourseReview.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: CourseReview): String =
        persistentObject.id.toString()

    override fun parsePersistentObject(cursor: Cursor): CourseReview =
        CourseReview(
            id      = cursor.getLong(DbStructureCourseReview.Columns.ID),
            course  = cursor.getLong(DbStructureCourseReview.Columns.COURSE),
            user    = cursor.getLong(DbStructureCourseReview.Columns.USER),
            score   = cursor.getInt(DbStructureCourseReview.Columns.SCORE),
            text    = cursor.getString(DbStructureCourseReview.Columns.TEXT),
            createDate = cursor.getDate(DbStructureCourseReview.Columns.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureCourseReview.Columns.UPDATE_DATE)
        )

    override fun getContentValues(persistentObject: CourseReview): ContentValues =
        ContentValues().apply {
            put(DbStructureCourseReview.Columns.ID, persistentObject.id)
            put(DbStructureCourseReview.Columns.COURSE, persistentObject.course)
            put(DbStructureCourseReview.Columns.USER, persistentObject.user)
            put(DbStructureCourseReview.Columns.SCORE, persistentObject.score)
            put(DbStructureCourseReview.Columns.TEXT, persistentObject.text)

            put(DbStructureCourseReview.Columns.CREATE_DATE, persistentObject.createDate?.time ?: -1)
            put(DbStructureCourseReview.Columns.UPDATE_DATE, persistentObject.updateDate?.time ?: -1)
        }
}