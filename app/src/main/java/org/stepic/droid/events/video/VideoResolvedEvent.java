package org.stepic.droid.events.video;

import org.stepic.droid.model.Video;

public class VideoResolvedEvent {
    Video mVideo;
    private String mPathToVideo;
    private final long stepId;

    public VideoResolvedEvent(Video video, String pathToVideo, long stepId) {
        this.mVideo = video;
        mPathToVideo = pathToVideo;
        this.stepId = stepId;
    }

    public String getPathToVideo() {
        return mPathToVideo;
    }

    public long getStepId() {
        return stepId;
    }
}
