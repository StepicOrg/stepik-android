package org.stepik.android.cache.discussion_thread.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getString
import org.stepik.android.cache.discussion_thread.structure.DbStructureDiscussionThread
import org.stepik.android.model.comments.DiscussionThread
import javax.inject.Inject

class DiscussionThreadDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<DiscussionThread>(databaseOperations) {
    override fun getDefaultPrimaryColumn(): String =
        DbStructureDiscussionThread.Columns.ID

    override fun getDbName(): String =
        DbStructureDiscussionThread.TABLE_NAME

    override fun getDefaultPrimaryValue(persistentObject: DiscussionThread): String =
        persistentObject.id

    override fun getContentValues(persistentObject: DiscussionThread): ContentValues =
        ContentValues(4)
            .apply {
                put(DbStructureDiscussionThread.Columns.ID, persistentObject.id)
                put(DbStructureDiscussionThread.Columns.THREAD, persistentObject.thread)
                put(DbStructureDiscussionThread.Columns.DISCUSSIONS_COUNT, persistentObject.discussionsCount)
                put(DbStructureDiscussionThread.Columns.DISCUSSION_PROXY, persistentObject.discussionProxy)
            }

    override fun parsePersistentObject(cursor: Cursor): DiscussionThread =
        DiscussionThread(
            cursor.getString(DbStructureDiscussionThread.Columns.ID)!!,
            cursor.getString(DbStructureDiscussionThread.Columns.THREAD)!!,
            cursor.getInt(DbStructureDiscussionThread.Columns.DISCUSSIONS_COUNT),
            cursor.getString(DbStructureDiscussionThread.Columns.DISCUSSION_PROXY)!!
        )
}