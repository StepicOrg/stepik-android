package org.stepic.droid.util

import org.stepic.droid.model.CachedVideo
import org.stepik.android.model.structure.Video
import org.stepik.android.model.structure.VideoUrl

fun CachedVideo?.transformToVideo(): Video? {
    var realVideo: Video? = null
    if (this != null) {
        val videoUrl = VideoUrl(this.url, this.quality)
        realVideo = Video(id = this.videoId, thumbnail = this.thumbnail, urls = listOf(videoUrl))
    }
    return realVideo
}
