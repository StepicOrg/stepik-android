package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.DbVideoUrl
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureVideoUrl
import javax.inject.Inject

class VideoUrlDaoImpl @Inject constructor(databaseOperations: DatabaseOperations) : DaoBase<DbVideoUrl>(databaseOperations) {

    override fun getDbName() = DbStructureVideoUrl.externalVideosName

    override fun getDefaultPrimaryColumn() = DbStructureVideoUrl.Column.videoId

    override fun getDefaultPrimaryValue(persistentObject: DbVideoUrl?) = 0.toString()

    override fun getContentValues(persistentObject: DbVideoUrl): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureVideoUrl.Column.videoId, persistentObject.videoId)
        contentValues.put(DbStructureVideoUrl.Column.url, persistentObject.url)
        contentValues.put(DbStructureVideoUrl.Column.quality, persistentObject.quality)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): DbVideoUrl {
        val indexVideoId = cursor.getColumnIndex(DbStructureVideoUrl.Column.videoId)
        val indexUrl = cursor.getColumnIndex(DbStructureVideoUrl.Column.url)
        val indexQuality = cursor.getColumnIndex(DbStructureVideoUrl.Column.quality)

        val videoId = cursor.getLong(indexVideoId)
        val url = cursor.getString(indexUrl)
        val quality = cursor.getString(indexQuality)

        return DbVideoUrl(
            videoId = videoId,
            url = url,
            quality = quality
        )
    }

}
