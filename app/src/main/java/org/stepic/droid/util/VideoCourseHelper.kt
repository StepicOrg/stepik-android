package org.stepic.droid.util

import org.stepic.droid.model.CachedVideo
import org.stepik.android.model.structure.Video

fun Video.transformToCachedVideo(): CachedVideo {
    val cachedVideo = CachedVideo()//it is cached, but not stored video.
    cachedVideo.videoId = this.id
    cachedVideo.stepId = -1
    cachedVideo.thumbnail = this.thumbnail

    this.urls?.let {
        if (it.isNotEmpty()) {
            val urlIndex = if (it.size > 1) {
                1 // workaround for HD video and high speed https://github.com/google/ExoPlayer/issues/2777
            } else {
                0
            }
            val videoUrl = it[urlIndex]
            cachedVideo.quality = videoUrl.quality
            cachedVideo.url = videoUrl.url
        }
    }

    return cachedVideo
}
