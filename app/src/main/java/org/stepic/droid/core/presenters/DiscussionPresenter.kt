package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.DiscussionView
import org.stepic.droid.di.comment.CommentsScope
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CommentsScope
class DiscussionPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api) : PresenterBase<DiscussionView>() {

    fun loadDiscussion(discussionId: String) {
        threadPoolExecutor.execute {
            try {
                val discussionProxy = api
                        .getDiscussionProxies(discussionId)
                        .execute()
                        .body()
                        .discussionProxies
                        .first()
                if (discussionProxy.discussions.isEmpty()) {
                    mainHandler.post {
                        view?.onEmptyComments(discussionProxy)
                    }
                } else {
                    mainHandler.post {
                        view?.onLoaded(discussionProxy)
                    }
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    view?.onInternetProblemInComments()
                }
            }
        }

    }

}
