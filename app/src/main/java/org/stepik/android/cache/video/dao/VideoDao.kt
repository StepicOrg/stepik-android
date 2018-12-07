package org.stepik.android.cache.video.dao

import org.stepik.android.model.Video

interface VideoDao {
    fun get(videoId: Long): Video?
    fun replace(video: Video)
    fun remove(videoId: Long)
}