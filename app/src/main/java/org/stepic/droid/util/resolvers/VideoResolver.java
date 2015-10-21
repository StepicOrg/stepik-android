package org.stepic.droid.util.resolvers;

import android.content.Context;

import com.squareup.otto.Bus;

import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.store.operations.DbOperationsCachedVideo;
import org.stepic.droid.util.AppConstants;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class VideoResolver implements IVideoResolver {


    private Context mContext;
    private Bus mBus;
    private DbOperationsCachedVideo mDbOperations;

    public VideoResolver(Context context, Bus bus, DbOperationsCachedVideo dbOperationsCachedVideo) {
        mContext = context;
        mBus = bus;
        mDbOperations = dbOperationsCachedVideo;
    }

    /**
     * Don't call in main thread
     *
     * @param video object video from step
     * @return path for video in web or local, null if video is incorrect or can't resolve
     */
    @Override
    public String resolveVideoUrl(final Video video) {
        //// TODO: 15.10.15 check in database by id, check availability of playing on the device, etc
//// TODO: 15.10.15 log all "return" statements

        if (video == null) return null;
        List<VideoUrl> urlList = video.getUrls();
        if (urlList == null || urlList.size() == 0) return null;

        String localPath = null;
        try {
            mDbOperations.open();
            localPath = mDbOperations.getPathIfExist(video);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mDbOperations.close();
        }

        if (localPath != null && checkExistingOnDisk(localPath)) {
            return localPath;
        } else {
            return resolveFromWeb(urlList);
        }
    }

    private String resolveFromWeb(List<VideoUrl> urlList) {
        String resolvedURL = null;
        for (int i = 0; i < urlList.size(); i++) {
            VideoUrl tempLink = urlList.get(i);
            if (tempLink != null) {
                String quality = tempLink.getQuality();
                if (quality != null &&
                        (quality.equals(AppConstants.DEFAULT_QUALITY) || i == urlList.size() - 1)) {
                    //// TODO: 15.10.15 determine video which is available for the phone. Not default
                    resolvedURL = tempLink.getUrl();
                    break;
                }
            }
        }

        if (resolvedURL != null) {
            return resolvedURL;
        } else
            return null;

    }

    private boolean checkExistingOnDisk(String path) {
        File downloadFolderAndFile = new File(path);
        if (downloadFolderAndFile.exists()) {
            return true;
        } else {
            try {
                mDbOperations.open();
                mDbOperations.deleteVideoByUrl(path);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                mDbOperations.close();
            }
            return false;
        }

    }

}
