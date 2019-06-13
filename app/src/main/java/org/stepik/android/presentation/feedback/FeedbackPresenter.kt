package org.stepik.android.presentation.feedback

import org.stepik.android.domain.feedback.interactor.FeedbackInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class FeedbackPresenter
@Inject
constructor(
    private val feedbackInteractor: FeedbackInteractor
) : PresenterBase<FeedbackView>() {
    fun sendTextFeedback(subject: String, aboutSystemInfo: String) {
        val emailUriData = feedbackInteractor.initUriData(subject, aboutSystemInfo)
        view?.sendTextFeedback(emailUriData)
    }
}