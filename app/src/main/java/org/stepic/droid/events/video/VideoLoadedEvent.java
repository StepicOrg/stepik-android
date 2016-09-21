package org.stepic.droid.events.video;

import org.stepic.droid.model.Video;

public class VideoLoadedEvent {
    private final Video video;
    private final long stepId;
    private final String videoUrl;


    public VideoLoadedEvent(Video video, long stepId, String videoUrl) {
        this.video = video;
        this.stepId = stepId;
        this.videoUrl = videoUrl;
    }

    public Video getVideo() {
        return video;
    }

    public long getStepId() {
        return stepId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
