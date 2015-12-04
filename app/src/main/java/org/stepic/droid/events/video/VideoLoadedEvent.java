package org.stepic.droid.events.video;

public class VideoLoadedEvent {
    private final String thumbnail;
    private final long stepId;
    private final String videoUrl;


    public VideoLoadedEvent(String thumbnail, long stepId, String videoUrl) {
        this.thumbnail = thumbnail;
        this.stepId = stepId;
        this.videoUrl = videoUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public long getStepId() {
        return stepId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
