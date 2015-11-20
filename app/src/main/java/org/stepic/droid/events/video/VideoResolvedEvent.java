package org.stepic.droid.events.video;

import org.stepic.droid.model.Video;

public class VideoResolvedEvent {
    Video mVideo;
    private String mPathToVideo;

    public VideoResolvedEvent(Video video, String pathToVideo) {
        this.mVideo = video;
        mPathToVideo = pathToVideo;
    }

//    public Video getVideo() {
//        return mVideo;
//    }

    public String getPathToVideo() {
        return mPathToVideo;
    }
}
