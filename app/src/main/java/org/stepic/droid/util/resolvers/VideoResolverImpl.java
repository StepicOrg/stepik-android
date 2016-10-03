package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.io.File;
import java.util.List;

public class VideoResolverImpl implements VideoResolver {


    private DatabaseFacade databaseFacade;
    private UserPreferences userPreferences;
    private CleanManager cleanManager;
    private Analytic analytic;

    public VideoResolverImpl(DatabaseFacade dbOperationsCachedVideo, UserPreferences userPreferences, CleanManager cleanManager, Analytic analytic) {
        databaseFacade = dbOperationsCachedVideo;
        this.userPreferences = userPreferences;
        this.cleanManager = cleanManager;
        this.analytic = analytic;
    }

    /**
     * Don't call in main thread
     *
     * @param video object video from step
     * @return path for video in web or local, null if video is incorrect or can't resolve
     */
    @Override
    public String resolveVideoUrl(@Nullable final Video video, Step step) {
        //// TODO: 15.10.15 check in database by id, check availability of playing on the device, etc
//// TODO: 15.10.15 log all "return" statements

        if (video == null) return null;

        String localPath = databaseFacade.getPathToVideoIfExist(video);

        if (localPath != null && checkExistingOnDisk(localPath, step)) {
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
            int weWant = Integer.parseInt(userPreferences.getQualityVideo());
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
            analytic.reportError(Analytic.Error.VIDEO_RESOLVER_FAILED, e);
            if (urlList == null || urlList.isEmpty()) return null;
            int upperBound = urlList.size() - 1;
            for (int i = upperBound; i >= 0; i--) {
                VideoUrl tempLink = urlList.get(i);
                if (tempLink != null) {
                    String quality = tempLink.getQuality();
                    if (quality != null &&
                            (quality.equals(userPreferences.getQualityVideo()))) {
                        resolvedURL = tempLink.getUrl();
                        break;
                    }
                }
            }
        }

        return resolvedURL;

    }

    private boolean checkExistingOnDisk(String path, Step step) {
        File downloadFolderAndFile = new File(path);
        if (downloadFolderAndFile.exists()) {
            return true;
        } else {
            cleanManager.removeStep(step);
            return false;
        }

    }

}
