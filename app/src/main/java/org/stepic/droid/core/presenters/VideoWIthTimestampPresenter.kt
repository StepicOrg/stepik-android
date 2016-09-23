package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class VideoWIthTimestampPresenter(val databaseFacade: DatabaseFacade,
                                  val mainHandler: IMainHandler,
                                  val threadPoolExecutor: ThreadPoolExecutor) : PresenterBase<VideoWithTimestampView>() {

    fun showVideoWithPredefinedTimestamp(videoId: Long) {
        threadPoolExecutor.execute {
            val timestamp: Long? = databaseFacade.getVideoTimestamp(videoId)?.timestamp
            mainHandler.post {
                view?.onNeedShowVideoWithTimestamp(timestamp)
            }
        }
    }

}
