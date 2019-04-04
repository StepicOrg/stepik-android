package org.stepik.android.cache.comments.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepik.android.cache.comments.structure.DbStructureCommentsBanner
import javax.inject.Inject

class CommentsBannerDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Long>(databaseOperations), CommentsBannerDao {
    override fun getDbName(): String =
        DbStructureCommentsBanner.COMMENTS_BANNER

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCommentsBanner.Columns.COURSE_ID

    override fun getDefaultPrimaryValue(persistentObject: Long?): String =
        persistentObject.toString()

    override fun getContentValues(persistentObject: Long?): ContentValues =
        ContentValues().apply {
            put(DbStructureCommentsBanner.Columns.COURSE_ID, persistentObject)
        }

    override fun parsePersistentObject(cursor: Cursor): Long =
        cursor.getLong(cursor.getColumnIndex(DbStructureCommentsBanner.Columns.COURSE_ID))
}