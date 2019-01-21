package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.di.video.VideoModule
import org.stepic.droid.di.video.VideoScope
import org.stepic.droid.model.VideoTimestamp
import org.stepic.droid.storage.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject
import javax.inject.Named

@VideoScope
class VideoWithTimestampPresenter
@Inject constructor(
    private val databaseFacade: DatabaseFacade,
    private val mainHandler: MainHandler,
    private val threadPoolExecutor: ThreadPoolExecutor,
    @Named(VideoModule.rewindOnOpenName)
    private val rewindOnOpenMillis: Long
) : PresenterBase<VideoWithTimestampView>() {

    private var cachedTimestamp: Long? = null

    fun showVideoWithPredefinedTimestamp(videoId: Long?) {
        if (cachedTimestamp != null) {
            //if some time exist in presenter -> show it.
            view?.onNeedShowVideoWithTimestamp(cachedTimestamp ?: 0L)
            return
        }

        if (videoId == null) {
            //if we do not have videoId -> it is not exist in database, then start with 0L
            view?.onNeedShowVideoWithTimestamp(0L)
            return
        }

        threadPoolExecutor.execute {
            val valueFromDatabase: Long = databaseFacade.getVideoTimestamp(videoId)?.timestamp ?: 0L
            val timestamp = maxOf(0L, valueFromDatabase - rewindOnOpenMillis)
            mainHandler.post {
                cachedTimestamp = timestamp
                view?.onNeedShowVideoWithTimestamp(timestamp)
            }
        }
    }

    fun saveMillis(currentTimeInMillis: Long, videoId: Long?) {
        if (currentTimeInMillis < 0) return
        val oldCachedTimeStamp = cachedTimestamp
        cachedTimestamp = currentTimeInMillis
        if (videoId == null || oldCachedTimeStamp == currentTimeInMillis) {
            //videoId null is saved only locally
            //if we already cache timestamp -> not cache it again
            return
        }
        threadPoolExecutor.execute {
            databaseFacade.addTimestamp(VideoTimestamp(videoId, currentTimeInMillis))
        }
    }

}
