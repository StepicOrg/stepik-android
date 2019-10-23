package org.stepik.android.cache.video_player

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.cache.video_player.model.VideoTimestamp
import org.stepik.android.data.video_player.source.VideoTimestampCacheDataSource
import javax.inject.Inject

class VideoTimestampCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : VideoTimestampCacheDataSource {

    override fun addVideoTimestamp(videoId: Long, timestamp: Long): Completable =
        Completable.fromCallable {
            databaseFacade.addTimestamp(
                VideoTimestamp(
                    videoId,
                    timestamp
                )
            )
        }

    override fun getVideoTimestamp(videoId: Long): Single<Long> =
        Single.fromCallable {
            databaseFacade
                .getVideoTimestamp(videoId)
                ?.timestamp
                ?: 0
        }
}