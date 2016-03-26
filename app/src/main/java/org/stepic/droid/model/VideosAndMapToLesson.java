package org.stepic.droid.model;

import java.util.List;
import java.util.Map;

public class VideosAndMapToLesson {
    private final List<CachedVideo> cachedVideoList;
    private final Map<Long, Lesson> mStepIdToLesson;

    public VideosAndMapToLesson(List<CachedVideo> cachedVideoList, Map<Long, Lesson> mStepIdToLesson) {
        this.cachedVideoList = cachedVideoList;
        this.mStepIdToLesson = mStepIdToLesson;
    }

    public List<CachedVideo> getCachedVideoList() {
        return cachedVideoList;
    }

    public Map<Long, Lesson> getStepIdToLesson() {
        return mStepIdToLesson;
    }
}
