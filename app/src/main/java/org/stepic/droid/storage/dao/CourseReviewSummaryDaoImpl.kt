package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getDouble
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.course.source.structure.DbStructureCourseReviewSummary
import org.stepik.android.model.CourseReviewSummary
import javax.inject.Inject

class CourseReviewSummaryDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<CourseReviewSummary>(databaseOperations) {
    override fun getDbName(): String =
        DbStructureCourseReviewSummary.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCourseReviewSummary.Columns.SUMMARY_ID

    override fun getDefaultPrimaryValue(persistentObject: CourseReviewSummary): String =
        persistentObject.average.toString()

    override fun parsePersistentObject(cursor: Cursor): CourseReviewSummary =
        CourseReviewSummary(
            id = cursor.getLong(DbStructureCourseReviewSummary.Columns.SUMMARY_ID),
            course = cursor.getLong(DbStructureCourseReviewSummary.Columns.COURSE_ID),
            average = cursor.getDouble(DbStructureCourseReviewSummary.Columns.AVERAGE),
            count = cursor.getInt(DbStructureCourseReviewSummary.Columns.COUNT),
            distribution = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourseReviewSummary.Columns.DISTRIBUTION)) ?: emptyList()
        )

    override fun getContentValues(persistentObject: CourseReviewSummary): ContentValues =
        ContentValues().apply {
            put(DbStructureCourseReviewSummary.Columns.SUMMARY_ID, persistentObject.id)
            put(DbStructureCourseReviewSummary.Columns.COURSE_ID, persistentObject.course)
            put(DbStructureCourseReviewSummary.Columns.AVERAGE, persistentObject.average)
            put(DbStructureCourseReviewSummary.Columns.COUNT, persistentObject.count)
            put(DbStructureCourseReviewSummary.Columns.DISTRIBUTION, DbParseHelper.parseLongListToString(persistentObject.distribution))
        }
}