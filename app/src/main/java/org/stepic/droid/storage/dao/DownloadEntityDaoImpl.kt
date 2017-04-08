package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.storage.structure.DbStructureSharedDownloads
import javax.inject.Inject

class DownloadEntityDaoImpl @Inject constructor(openHelper: SQLiteDatabase) : DaoBase<DownloadEntity>(openHelper) {

    public override fun parsePersistentObject(cursor: Cursor): DownloadEntity {
        val indexDownloadId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.DOWNLOAD_ID)
        val indexStepId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.STEP_ID)
        val indexVideoId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.VIDEO_ID)
        val indexThumbnail = cursor.getColumnIndex(DbStructureSharedDownloads.Column.THUMBNAIL)
        val indexQuality = cursor.getColumnIndex(DbStructureSharedDownloads.Column.QUALITY)

        return DownloadEntity(
                downloadId = cursor.getLong(indexDownloadId),
                stepId = cursor.getLong(indexStepId),
                videoId = cursor.getLong(indexVideoId),
                thumbnail = cursor.getString(indexThumbnail),
                quality = cursor.getString(indexQuality)
        )
    }

    public override fun getDbName(): String {
        return DbStructureSharedDownloads.SHARED_DOWNLOADS
    }

    public override fun getContentValues(downloadEntity: DownloadEntity): ContentValues {
        val values = ContentValues()

        values.put(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadEntity.downloadId)
        values.put(DbStructureSharedDownloads.Column.VIDEO_ID, downloadEntity.videoId)
        values.put(DbStructureSharedDownloads.Column.STEP_ID, downloadEntity.stepId)
        values.put(DbStructureSharedDownloads.Column.THUMBNAIL, downloadEntity.thumbnail)
        values.put(DbStructureSharedDownloads.Column.QUALITY, downloadEntity.quality)

        return values
    }

    public override fun getDefaultPrimaryColumn(): String {
        return DbStructureSharedDownloads.Column.DOWNLOAD_ID
    }

    public override fun getDefaultPrimaryValue(persistentObject: DownloadEntity): String {
        return persistentObject.downloadId.toString() + ""
    }
}
