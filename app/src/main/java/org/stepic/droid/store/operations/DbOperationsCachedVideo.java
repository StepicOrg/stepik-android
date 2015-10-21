package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Video;
import org.stepic.droid.store.structure.DbStructureCachedVideo;

import java.util.ArrayList;
import java.util.List;

public class DbOperationsCachedVideo extends DbOperationsBase {

    public DbOperationsCachedVideo(Context context) {
        super(context);
    }



    @Override
    public Cursor getCursor() {
        return database.query(DbStructureCachedVideo.CACHED_VIDEO, DbStructureCachedVideo.getUsedColumns(),
                null, null, null, null, null);
    }

    public void addVideo(CachedVideo cachedVideo) {
        ContentValues values = new ContentValues();

        values.put(DbStructureCachedVideo.Column.VIDEO_ID, cachedVideo.getUrl());
        values.put(DbStructureCachedVideo.Column.VIDEO_ID, cachedVideo.getUrl());

        database.insert(DbStructureCachedVideo.CACHED_VIDEO, null, values);
    }

    private boolean isVideoInDb(Video video) {
        String Query = "Select * from " + DbStructureCachedVideo.CACHED_VIDEO + " where " + DbStructureCachedVideo.Column.VIDEO_ID + " = " + video.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void deleteVideo(Video video) {
        long videoId = video.getId();
        database.delete(DbStructureCachedVideo.CACHED_VIDEO,
                DbStructureCachedVideo.Column.VIDEO_ID + " = " + videoId,
                null);
    }

    public void deleteVideoByUrl(String path) {
        database.delete(DbStructureCachedVideo.CACHED_VIDEO,
                DbStructureCachedVideo.Column.URL + " = " + path,
                null);
    }

    public List<String> getPathsForAllCachedVideo() {
        List<String> cachedPaths = new ArrayList<>();

        Cursor cursor = getCursor();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            CachedVideo cachedVideo = parseCachedVideo(cursor);
            cachedPaths.add(cachedVideo.getUrl());
            cursor.moveToNext();
        }

        cursor.close();
        return cachedPaths;
    }

    public CachedVideo parseCachedVideo(Cursor cursor) {
        CachedVideo cachedVideo = new CachedVideo();
        int columnNumber = 0;
        cachedVideo.setVideoId(cursor.getLong(columnNumber++));
        cachedVideo.setUrl(cursor.getString(columnNumber++));
        return cachedVideo;
    }


    @Override
    public void clearCache() {
        List<String> paths = getPathsForAllCachedVideo();
        for (String pathItem : paths) {
            deleteVideoByUrl(pathItem);
        }
    }

    /**
     * getPath of cached video
     *
     * @param video video which we check for contains in db
     * @return null if video not existing in db, otherwise path to disk
     */
    public String getPathIfExist(Video video) {
        String Query = "Select * from " + DbStructureCachedVideo.CACHED_VIDEO + " where " + DbStructureCachedVideo.Column.VIDEO_ID + " = " + video.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return null;
        }
        int columnNumberOfPath = 1;
        String path = cursor.getString(columnNumberOfPath);
        cursor.close();
        return path;
    }

}
