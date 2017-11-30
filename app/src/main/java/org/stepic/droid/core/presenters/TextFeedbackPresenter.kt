package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.TextFeedbackView
import org.stepic.droid.di.feedback.FeedbackScope
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@FeedbackScope
class TextFeedbackPresenter
@Inject constructor(
        private val mainHandler: MainHandler,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val api: Api,
        private val analytic: Analytic
) : PresenterBase<TextFeedbackView>() {

    fun sendFeedback(email: String, description: String) {
        threadPoolExecutor.execute {
            try {
                val response = api.sendFeedback(email, description).execute()
                if (response.isSuccessful) {
                    analytic.reportEvent(Analytic.Feedback.FEEDBACK_SENT)
                    mainHandler.post { view?.onFeedbackSent() }
                } else {
                    analytic.reportEvent(Analytic.Error.FEEDBACK_BROKEN)
                    mainHandler.post { view?.onServerFail() }
                }
            } catch (exception: Exception) {
                mainHandler.post { view?.onInternetProblems() }
            }
        }
    }

}
