package org.stepik.android.data.video_player.source

import io.reactivex.Completable
import io.reactivex.Single

interface VideoTimestampCacheDataSource {
    fun addVideoTimestamp(videoId: Long, timestamp: Long): Completable
    fun getVideoTimestamp(videoId: Long): Single<Long>
}