package org.stepic.droid.util

import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.Video

object VideoCourseHelper {

    fun Video.transformToCachedVideo(): CachedVideo {
        val cachedVideo = CachedVideo()//it is cached, but not stored video.
        cachedVideo.videoId = this.id
        cachedVideo.stepId = -1
        cachedVideo.thumbnail = this.thumbnail
        if (this.urls != null && !this.urls.isEmpty()) {
            val urlIndex = if (this.urls.size > 1) {
                1 // workaround for HD video and high speed https://github.com/google/ExoPlayer/issues/2777
            } else {
                0
            }
            val videoUrl = this.urls[urlIndex]
            cachedVideo.quality = videoUrl.quality
            cachedVideo.url = videoUrl.url
        }
        return cachedVideo
    }
}
