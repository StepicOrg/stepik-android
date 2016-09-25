package org.stepic.droid.events.video;

public class VideoResolvedEvent {
    private long videoId;
    private String pathToVideo;
    private final long stepId;

    public VideoResolvedEvent(long videoId, String pathToVideo, long stepId) {
        this.videoId = videoId;
        this.pathToVideo = pathToVideo;
        this.stepId = stepId;
    }

    public String getPathToVideo() {
        return pathToVideo;
    }

    public long getStepId() {
        return stepId;
    }

    public long getVideoId() {
        return videoId;
    }
}
