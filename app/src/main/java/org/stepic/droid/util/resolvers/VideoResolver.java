package org.stepic.droid.util.resolvers;

import android.content.Context;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.io.File;
import java.util.List;

public class VideoResolver implements IVideoResolver {


    private Context mContext;
    private Bus mBus;
    private DatabaseFacade mDbOperations;
    private UserPreferences mUserPreferences;

    public VideoResolver(Context context, Bus bus, DatabaseFacade dbOperationsCachedVideo, UserPreferences userPreferences) {
        mContext = context;
        mBus = bus;
        mDbOperations = dbOperationsCachedVideo;
        mUserPreferences = userPreferences;
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

    @Nullable
    private String resolveFromWeb(List<VideoUrl> urlList) {
        String resolvedURL = null;


        try {
            int weWant = Integer.parseInt(mUserPreferences.getQualityVideo());
            int bestDelta = Integer.MAX_VALUE;
            int bestIndex = 0;
            for (int i = 0; i < urlList.size(); i++) {
                int current = Integer.parseInt(urlList.get(i).getQuality());
                int delta = Math.abs(current - weWant);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestIndex = i;
                }

            }
            resolvedURL = urlList.get(bestIndex).getUrl();
        } catch (NumberFormatException e) {
            //this is approach in BAD case
            YandexMetrica.reportError("video resolver is failed", e);
            if (urlList == null || urlList.isEmpty()) return null;
            int upperBound = urlList.size() - 1;
            for (int i = upperBound; i >= 0; i--) {
                VideoUrl tempLink = urlList.get(i);
                if (tempLink != null) {
                    String quality = tempLink.getQuality();
                    if (quality != null &&
                            (quality.equals(mUserPreferences.getQualityVideo()))) {
                        resolvedURL = tempLink.getUrl();
                        break;
                    }
                }
            }
        }

        return resolvedURL;

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
