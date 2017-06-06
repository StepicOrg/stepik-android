package org.stepic.droid.util

import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.Video
import org.stepic.droid.model.VideoUrl

fun CachedVideo?.transformToVideo(): Video? {
    var realVideo: Video? = null
    if (this != null) {
        realVideo = Video()
        realVideo.id = this.videoId
        realVideo.thumbnail = this.thumbnail
        val videoUrl = VideoUrl()
        videoUrl.quality = this.quality
        videoUrl.url = this.url

        val list = ArrayList<VideoUrl>()
        list.add(videoUrl)
        realVideo.urls = list
    }
    return realVideo
}
