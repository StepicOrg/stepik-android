package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.storage.structure.DbStructureCachedVideo;

import javax.inject.Inject;

public class PersistentVideoDaoImpl extends DaoBase<CachedVideo> {

    @Inject
    public PersistentVideoDaoImpl(SQLiteDatabase openHelper) {
        super(openHelper);
    }

    @Override
    public CachedVideo parsePersistentObject(Cursor cursor) {
        CachedVideo cachedVideo = new CachedVideo();
        int indexStepId = cursor.getColumnIndex(DbStructureCachedVideo.Column.STEP_ID);
        int indexVideoId = cursor.getColumnIndex(DbStructureCachedVideo.Column.VIDEO_ID);
        int indexUrl = cursor.getColumnIndex(DbStructureCachedVideo.Column.URL);
        int indexThumbnail = cursor.getColumnIndex(DbStructureCachedVideo.Column.THUMBNAIL);
        int indexQuality = cursor.getColumnIndex(DbStructureCachedVideo.Column.QUALITY);

        cachedVideo.setVideoId(cursor.getLong(indexVideoId));
        cachedVideo.setUrl(cursor.getString(indexUrl));
        cachedVideo.setThumbnail(cursor.getString(indexThumbnail));
        cachedVideo.setStepId(cursor.getLong(indexStepId));
        cachedVideo.setQuality(cursor.getString(indexQuality));
        return cachedVideo;
    }

    @Override
    public String getDbName() {
        return DbStructureCachedVideo.CACHED_VIDEO;
    }

    @Override
    public ContentValues getContentValues(CachedVideo cachedVideo) {
        ContentValues values = new ContentValues();

        values.put(DbStructureCachedVideo.Column.VIDEO_ID, cachedVideo.getVideoId());
        values.put(DbStructureCachedVideo.Column.STEP_ID, cachedVideo.getStepId());
        values.put(DbStructureCachedVideo.Column.URL, cachedVideo.getUrl());
        values.put(DbStructureCachedVideo.Column.THUMBNAIL, cachedVideo.getThumbnail());
        values.put(DbStructureCachedVideo.Column.QUALITY, cachedVideo.getQuality());

        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureCachedVideo.Column.VIDEO_ID;
    }

    @Override
    public String getDefaultPrimaryValue(CachedVideo persistentObject) {
        return persistentObject.getVideoId()+"";
    }
}
