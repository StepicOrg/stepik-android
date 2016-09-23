package org.stepic.droid.events.video;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Video;

public class VideoResolvedEvent {
    @NotNull
    private Video video;
    private String pathToVideo;
    private final long stepId;

    public VideoResolvedEvent(@NotNull Video video, String pathToVideo, long stepId) {
        this.video = video;
        this.pathToVideo = pathToVideo;
        this.stepId = stepId;
    }

    public String getPathToVideo() {
        return pathToVideo;
    }

    public long getStepId() {
        return stepId;
    }

    public long getVideoId(){
        return video.getId();
    }
}
