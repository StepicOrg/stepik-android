package org.stepik.android.cache.video.dao

import org.stepik.android.model.Video

interface VideoDao {
    fun getVideo(videoId: Long): Video?
    fun saveVideo(video: Video)
}