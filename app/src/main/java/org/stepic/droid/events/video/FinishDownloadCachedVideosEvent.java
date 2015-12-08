package org.stepic.droid.events.video;

import org.stepic.droid.model.CachedVideo;

import java.util.List;

public class FinishDownloadCachedVideosEvent {
    List<CachedVideo> cachedVideos;
    public FinishDownloadCachedVideosEvent (List<CachedVideo> list) {
        cachedVideos = list;
    }

    public List<CachedVideo> getCachedVideos() {
        return cachedVideos;
    }
}
