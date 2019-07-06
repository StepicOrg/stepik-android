package org.stepik.android.cache.comment_banner.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner
import javax.inject.Inject

class CommentBannerDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Long>(databaseOperations),
    CommentBannerDao {
    override fun getDbName(): String =
        DbStructureCommentBanner.COMMENTS_BANNER

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCommentBanner.Columns.COURSE_ID

    override fun getDefaultPrimaryValue(persistentObject: Long?): String =
        persistentObject.toString()

    override fun getContentValues(persistentObject: Long?): ContentValues =
        ContentValues().apply {
            put(DbStructureCommentBanner.Columns.COURSE_ID, persistentObject)
        }

    override fun parsePersistentObject(cursor: Cursor): Long =
        cursor.getLong(cursor.getColumnIndex(DbStructureCommentBanner.Columns.COURSE_ID))
}