package org.stepik.android.cache.video.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.video.model.VideoEntity
import org.stepik.android.cache.video.structure.VideoDbScheme
import javax.inject.Inject

class VIdeoEntityDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<VideoEntity>(databaseOperations) {
    override fun getDbName(): String =
        VideoDbScheme.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        VideoDbScheme.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: VideoEntity): String =
        persistentObject.id.toString()

    override fun getContentValues(persistentObject: VideoEntity): ContentValues =
        ContentValues().apply {
            put(VideoDbScheme.Columns.ID, persistentObject.id)
            put(VideoDbScheme.Columns.DURATION, persistentObject.duration)
            put(VideoDbScheme.Columns.THUMBNAIL, persistentObject.thumbnail)
        }

    override fun parsePersistentObject(cursor: Cursor): VideoEntity =
        VideoEntity(
            id        = cursor.getLong(VideoDbScheme.Columns.ID),
            duration  = cursor.getLong(VideoDbScheme.Columns.DURATION),
            thumbnail = cursor.getString(VideoDbScheme.Columns.THUMBNAIL)
        )
}