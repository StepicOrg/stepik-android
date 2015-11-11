package org.stepic.droid.util.resolvers;

import android.content.Context;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;

import java.io.File;
import java.util.List;

public class VideoResolver implements IVideoResolver {


    private Context mContext;
    private Bus mBus;
    private DatabaseManager mDbOperations;

    public VideoResolver(Context context, Bus bus, DatabaseManager dbOperationsCachedVideo) {
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
    public String resolveVideoUrl(@Nullable final Video video) {
        //// TODO: 15.10.15 check in database by id, check availability of playing on the device, etc
//// TODO: 15.10.15 log all "return" statements

        if (video == null) return null;

        String localPath = mDbOperations.getPathToVideoIfExist(video);

        if (localPath != null && checkExistingOnDisk(localPath)) {
            return localPath;
        } else {
            List<VideoUrl> urlList = video.getUrls();
            if (urlList == null || urlList.size() == 0) return null;
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
            mDbOperations.deleteVideoByUrl(path);
            return false;
        }

    }

}
