package org.stepic.droid.events.video;

import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;

import java.util.List;
import java.util.Map;

public class FinishDownloadCachedVideosEvent {
    List<CachedVideo> cachedVideos;
    private Map<Long, Lesson> map;

    public FinishDownloadCachedVideosEvent(List<CachedVideo> list, Map<Long, Lesson> map) {
        cachedVideos = list;
        this.map = map;
    }

    public Map<Long, Lesson> getMap() {
        return map;
    }

    public List<CachedVideo> getCachedVideos() {
        return cachedVideos;
    }
}
