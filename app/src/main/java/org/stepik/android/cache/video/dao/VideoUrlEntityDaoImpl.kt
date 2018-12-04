package org.stepik.android.cache.video.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.video.model.VideoUrlEntity
import org.stepik.android.cache.video.structure.VideoUrlDbScheme
import javax.inject.Inject

class VideoUrlEntityDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<VideoUrlEntity>(databaseOperations) {
    override fun getDbName(): String =
        VideoUrlDbScheme.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        VideoUrlDbScheme.Columns.VIDEO_ID

    override fun getDefaultPrimaryValue(persistentObject: VideoUrlEntity): String =
        persistentObject.videoId.toString()

    override fun getContentValues(persistentObject: VideoUrlEntity): ContentValues =
        ContentValues().apply {
            put(VideoUrlDbScheme.Columns.VIDEO_ID, persistentObject.videoId)
            put(VideoUrlDbScheme.Columns.URL, persistentObject.url)
            put(VideoUrlDbScheme.Columns.QUALITY, persistentObject.quality)
        }

    override fun parsePersistentObject(cursor: Cursor): VideoUrlEntity =
        VideoUrlEntity(
            videoId = cursor.getLong(VideoUrlDbScheme.Columns.VIDEO_ID),
            url     = cursor.getString(VideoUrlDbScheme.Columns.URL)!!,
            quality = cursor.getString(VideoUrlDbScheme.Columns.QUALITY)!!
        )

}