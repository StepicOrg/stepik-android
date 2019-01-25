package org.stepik.android.domain.video_player.repository

import io.reactivex.Completable
import io.reactivex.Single

interface VideoTimestampRepository {
    fun addVideoTimestamp(videoId: Long, timestamp: Long): Completable
    fun getVideoTimestamp(videoId: Long): Single<Long>
}