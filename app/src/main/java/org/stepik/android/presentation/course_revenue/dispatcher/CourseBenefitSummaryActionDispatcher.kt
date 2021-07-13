package org.stepik.android.presentation.course_revenue.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_revenue.interactor.CourseBenefitsInteractor
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseBenefitSummaryActionDispatcher
@Inject
constructor(
    private val courseBenefitsInteractor: CourseBenefitsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseBenefitSummaryFeature.Action, CourseBenefitSummaryFeature.Message>() {
    override fun handleAction(action: CourseBenefitSummaryFeature.Action) {
        when (action) {
            is CourseBenefitSummaryFeature.Action.FetchCourseBenefitSummary -> {
                compositeDisposable += courseBenefitsInteractor
                    .getCourseBenefitSummary(action.courseId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseBenefitSummaryFeature.Message.FetchCourseBenefitSummarySuccess(it)) },
                        onComplete = { onNewMessage(CourseBenefitSummaryFeature.Message.FetchCourseBenefitSummarySuccess(CourseBenefitSummary.EMPTY)) },
                        onError = { onNewMessage(CourseBenefitSummaryFeature.Message.FetchCourseBenefitSummaryFailure) }
                    )
            }
        }
    }
}