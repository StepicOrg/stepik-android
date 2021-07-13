package org.stepik.android.presentation.course_revenue.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_revenue.interactor.CourseBenefitsInteractor
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseBenefitsMonthlyActionDispatcher
@Inject
constructor(
    private val courseBenefitsInteractor: CourseBenefitsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseBenefitsMonthlyFeature.Action, CourseBenefitsMonthlyFeature.Message>() {
    override fun handleAction(action: CourseBenefitsMonthlyFeature.Action) {
        when (action) {
            is CourseBenefitsMonthlyFeature.Action.FetchCourseBenefitsByMonths -> {
                compositeDisposable += courseBenefitsInteractor
                    .getCourseBenefitsByMonths(action.courseId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseBenefitsMonthlyFeature.Message.FetchCourseBenefitsByMonthsSuccess(it)) },
                        onError = { onNewMessage(CourseBenefitsMonthlyFeature.Message.FetchCourseBenefitsByMonthsFailure) }
                    )
            }
        }
    }
}