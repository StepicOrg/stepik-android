package org.stepik.android.presentation.feedback

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.feedback.interactor.FeedbackInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class FeedbackPresenter
@Inject
constructor(
    private val feedbackInteractor: FeedbackInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<FeedbackView>() {
    fun sendTextFeedback(subject: String, aboutSystemInfo: String) {
        compositeDisposable += feedbackInteractor
            .createSupportEmailData(subject, aboutSystemInfo)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { view?.sendTextFeedback(it) },
                onError = emptyOnErrorStub
            )
    }
}