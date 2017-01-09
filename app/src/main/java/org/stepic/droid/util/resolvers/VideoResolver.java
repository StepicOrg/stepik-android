package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Video;

public interface VideoResolver {

    /**
     * Resolve Video and return path for the video (from web or SD, Internal Storage)
     * <p/>
     *
     * @param video object video from step
     */
    @Nullable
    String resolveVideoUrl(@Nullable Video video, Step step);
}
