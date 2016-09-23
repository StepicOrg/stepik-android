package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class VideoWIthTimestampPresenter(val databaseFacade: DatabaseFacade,
                                  val mainHandler: IMainHandler,
                                  val threadPoolExecutor: ThreadPoolExecutor) : PresenterBase<VideoWithTimestampView>() {

    var cachedTimestamp: Long? = null
        private set

    fun showVideoWithPredefinedTimestamp(videoId: Long?) {
        if (videoId == null) {
            view?.onNeedShowVideoWithTimestamp(null)
            return
        }

        if (cachedTimestamp != null) {
            view?.onNeedShowVideoWithTimestamp(cachedTimestamp)
        }


        threadPoolExecutor.execute {
//            val timestamp: Long? = databaseFacade.getVideoTimestamp(videoId)?.timestamp
            val timestamp: Long? = 430000L
            mainHandler.post {
                cachedTimestamp = timestamp
                view?.onNeedShowVideoWithTimestamp(timestamp)
            }
        }
    }

}
