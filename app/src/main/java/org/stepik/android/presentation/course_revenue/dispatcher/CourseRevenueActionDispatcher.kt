package org.stepik.android.presentation.course_revenue.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.feedback.interactor.FeedbackInteractor
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseRevenueActionDispatcher
@Inject
constructor(
    private val feedbackInteractor: FeedbackInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseRevenueFeature.Action, CourseRevenueFeature.Message>() {
    override fun handleAction(action: CourseRevenueFeature.Action) {
        when (action) {
            is CourseRevenueFeature.Action.GenerateSupportEmailData -> {
                compositeDisposable += feedbackInteractor
                    .createSupportEmailData(action.subject, action.deviceInfo)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseRevenueFeature.Message.SetupFeedbackSuccess(it)) },
                        onError = emptyOnErrorStub
                    )
            }
        }
    }
}