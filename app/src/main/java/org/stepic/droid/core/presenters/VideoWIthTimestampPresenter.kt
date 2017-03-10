package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.model.VideoTimestamp
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class VideoWIthTimestampPresenter(val databaseFacade: DatabaseFacade,
                                  val mainHandler: MainHandler,
                                  val threadPoolExecutor: ThreadPoolExecutor) : PresenterBase<VideoWithTimestampView>() {

    var cachedTimestamp: Long? = null
        private set

    fun showVideoWithPredefinedTimestamp(videoId: Long?) {
        if (videoId == null) {
            view?.onNeedShowVideoWithTimestamp(null ?: 0L)
            return
        }

        if (cachedTimestamp != null) {
            view?.onNeedShowVideoWithTimestamp(cachedTimestamp ?: 0L)
        }


        threadPoolExecutor.execute {
            val timestamp: Long? = databaseFacade.getVideoTimestamp(videoId)?.timestamp
            mainHandler.post {
                cachedTimestamp = timestamp
                view?.onNeedShowVideoWithTimestamp(timestamp ?: 0L)
            }
        }
    }

    fun saveMillis(currentTimeInMillis: Long, videoId: Long?) {
        if (videoId == null || currentTimeInMillis <= 0) return
        threadPoolExecutor.execute {
            databaseFacade.addTimestamp(VideoTimestamp(videoId, currentTimeInMillis))
        }
    }

}
