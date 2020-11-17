package org.stepik.android.cache.course_collection.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<CourseCollection>(databaseOperations) {
    override fun getDefaultPrimaryColumn(): String =
        DbStructureCourseCollection.Columns.ID

    override fun getDbName(): String =
        DbStructureCourseCollection.TABLE_NAME

    override fun getDefaultPrimaryValue(persistentObject: CourseCollection): String =
        persistentObject.id.toString()

    override fun getContentValues(persistentObject: CourseCollection): ContentValues =
        ContentValues(7)
            .apply {
                put(DbStructureCourseCollection.Columns.ID, persistentObject.id)
                put(DbStructureCourseCollection.Columns.POSITION, persistentObject.position)
                put(DbStructureCourseCollection.Columns.TITLE, persistentObject.title)
                put(DbStructureCourseCollection.Columns.LANGUAGE, persistentObject.language)
                put(DbStructureCourseCollection.Columns.COURSES, DbParseHelper.parseLongListToString(persistentObject.courses))
                put(DbStructureCourseCollection.Columns.DESCRIPTION, persistentObject.description)
                put(DbStructureCourseCollection.Columns.PLATFORM, persistentObject.platform)
            }

    override fun parsePersistentObject(cursor: Cursor): CourseCollection =
        CourseCollection(
            cursor.getLong(DbStructureCourseCollection.Columns.ID),
            cursor.getInt(DbStructureCourseCollection.Columns.POSITION),
            cursor.getString(DbStructureCourseCollection.Columns.TITLE)!!,
            cursor.getString(DbStructureCourseCollection.Columns.LANGUAGE)!!,
            DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourseCollection.Columns.COURSES)) ?: listOf(),
            cursor.getString(DbStructureCourseCollection.Columns.DESCRIPTION)!!,
            cursor.getInt(DbStructureCourseCollection.Columns.PLATFORM)
        )
}