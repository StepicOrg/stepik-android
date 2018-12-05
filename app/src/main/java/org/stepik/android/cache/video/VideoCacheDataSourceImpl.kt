package org.stepik.android.cache.video

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.video.dao.VideoDao
import org.stepik.android.model.Video
import javax.inject.Inject

class VideoCacheDataSourceImpl
@Inject
constructor(
    private val videoDao: VideoDao
) {
    fun getVideo(videoId: Long): Maybe<Video> =
        Maybe.create { emitter ->
            videoDao.get(videoId)
                ?.let(emitter::onSuccess)
                ?: emitter.onComplete()
        }

    fun saveVideo(video: Video): Completable =
        Completable.fromAction {
            videoDao.replace(video)
        }
}