package org.stepic.droid.util.resolvers;

import org.stepic.droid.model.Video;

public interface IVideoResolver {

    /**
     * Resolve Video and return path for the video (from web or SD, Internal Storage)
     * <p/>
     * arise {@see org.stepic.droid.events.video.VideoResolvedEvent}, when is successful
     *
     * @param video object video from step
     */
    void resolveVideoUrl(Video video);
}
