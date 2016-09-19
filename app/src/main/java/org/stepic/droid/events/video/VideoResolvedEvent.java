package org.stepic.droid.events.video;

import org.stepic.droid.model.Video;

public class VideoResolvedEvent {
    Video video;
    private String pathToVideo;
    private final long stepId;

    public VideoResolvedEvent(Video video, String pathToVideo, long stepId) {
        this.video = video;
        this.pathToVideo = pathToVideo;
        this.stepId = stepId;
    }

//    public Video getVideo() {
//        return mVideo;
//    }

    public String getPathToVideo() {
        return pathToVideo;
    }

    public long getStepId() {
        return stepId;
    }
}
