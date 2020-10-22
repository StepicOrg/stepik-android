package org.stepik.android.cache.course_list.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getString
import org.stepik.android.cache.course_list.structure.DbStructureCourseListQuery
import org.stepik.android.data.course_list.model.CourseListQueryData
import javax.inject.Inject

class CourseListQueryDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<CourseListQueryData>(databaseOperations) {
    override fun getDefaultPrimaryColumn(): String =
        DbStructureCourseListQuery.Columns.ID

    override fun getDbName(): String =
        DbStructureCourseListQuery.TABLE_NAME

    override fun getDefaultPrimaryValue(persistentObject: CourseListQueryData): String =
        persistentObject.courseListQueryId

    override fun getContentValues(persistentObject: CourseListQueryData): ContentValues =
        ContentValues(2).apply {
            put(DbStructureCourseListQuery.Columns.ID, persistentObject.courseListQueryId)
            put(DbStructureCourseListQuery.Columns.COURSES, DbParseHelper.parseLongListToString(persistentObject.courses))
        }

    override fun parsePersistentObject(cursor: Cursor): CourseListQueryData =
        CourseListQueryData(
            cursor.getString(DbStructureCourseListQuery.Columns.ID)!!,
            DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourseListQuery.Columns.COURSES)) ?: listOf()
        )
}