package org.stepik.android.data.video_player.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.video_player.source.VideoTimestampCacheDataSource
import org.stepik.android.domain.video_player.repository.VideoTimestampRepository
import javax.inject.Inject

class VideoTimestampRepositoryImpl
@Inject
constructor(
    private val videoTimestampCacheDataSource: VideoTimestampCacheDataSource
) : VideoTimestampRepository {

    override fun addVideoTimestamp(videoId: Long, timestamp: Long): Completable =
        videoTimestampCacheDataSource.addVideoTimestamp(videoId, timestamp)

    override fun getVideoTimestamp(videoId: Long): Single<Long> =
        videoTimestampCacheDataSource.getVideoTimestamp(videoId)
}