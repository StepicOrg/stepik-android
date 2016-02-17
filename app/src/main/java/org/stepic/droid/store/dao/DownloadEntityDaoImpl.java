package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.store.structure.DbStructureSharedDownloads;

public class DownloadEntityDaoImpl extends DaoBase<DownloadEntity> {
    public DownloadEntityDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
    }

    @Override
    public DownloadEntity parsePersistentObject(Cursor cursor) {
        DownloadEntity downloadEntity = new DownloadEntity();

        int indexDownloadId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.DOWNLOAD_ID);
        int indexStepId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.STEP_ID);
        int indexVideoId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.VIDEO_ID);
        int indexThumbnail = cursor.getColumnIndex(DbStructureSharedDownloads.Column.THUMBNAIL);
        int indexQuality = cursor.getColumnIndex(DbStructureSharedDownloads.Column.QUALITY);

        downloadEntity.setDownloadId(cursor.getLong(indexDownloadId));
        downloadEntity.setStepId(cursor.getLong(indexStepId));
        downloadEntity.setVideoId(cursor.getLong(indexVideoId));
        downloadEntity.setThumbnail(cursor.getString(indexThumbnail));
        downloadEntity.setQuality(cursor.getString(indexQuality));

        return downloadEntity;
    }

    @Override
    public String getDbName() {
        return DbStructureSharedDownloads.SHARED_DOWNLOADS;
    }

    @Override
    public ContentValues getContentValues(DownloadEntity downloadEntity) {
        ContentValues values = new ContentValues();

        values.put(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadEntity.getDownloadId());
        values.put(DbStructureSharedDownloads.Column.VIDEO_ID, downloadEntity.getVideoId());
        values.put(DbStructureSharedDownloads.Column.STEP_ID, downloadEntity.getStepId());
        values.put(DbStructureSharedDownloads.Column.THUMBNAIL, downloadEntity.getThumbnail());
        values.put(DbStructureSharedDownloads.Column.QUALITY, downloadEntity.getQuality());

        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureSharedDownloads.Column.DOWNLOAD_ID;
    }

    @Override
    public String getDefaultPrimaryValue(DownloadEntity persistentObject) {
        return persistentObject.getDownloadId() + "";
    }
}
