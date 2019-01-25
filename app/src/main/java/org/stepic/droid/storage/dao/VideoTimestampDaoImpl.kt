package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepik.android.cache.video_player.model.VideoTimestamp
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureVideoTimestamp
import javax.inject.Inject

class VideoTimestampDaoImpl @Inject constructor(databaseOperations: DatabaseOperations) : DaoBase<VideoTimestamp>(databaseOperations) {

    public override fun getDbName() =
            DbStructureVideoTimestamp.VIDEO_TIMESTAMP

    public override fun getDefaultPrimaryColumn() =
            DbStructureVideoTimestamp.Column.VIDEO_ID

    public override fun getDefaultPrimaryValue(persistentObject: VideoTimestamp) =
            persistentObject.videoId.toString()

    public override fun getContentValues(persistentObject: VideoTimestamp): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureVideoTimestamp.Column.VIDEO_ID, persistentObject.videoId)
        contentValues.put(DbStructureVideoTimestamp.Column.TIMESTAMP, persistentObject.timestamp)
        return contentValues
    }

    public override fun parsePersistentObject(cursor: Cursor): VideoTimestamp {
        val indexVideoId = cursor.getColumnIndex(DbStructureVideoTimestamp.Column.VIDEO_ID)
        val indexTimestamp = cursor.getColumnIndex(DbStructureVideoTimestamp.Column.TIMESTAMP)

        val videoId: Long = cursor.getLong(indexVideoId)
        val timestamp: Long = cursor.getLong(indexTimestamp)

        return VideoTimestamp(
            videoId,
            timestamp
        )

    }
}
