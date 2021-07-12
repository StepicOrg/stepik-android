package org.stepik.android.presentation.course_revenue.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_revenue.interactor.CourseBenefitsInteractor
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseBenefitsActionDispatcher
@Inject
constructor(
    private val courseBenefitsInteractor: CourseBenefitsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseBenefitsFeature.Action, CourseBenefitsFeature.Message>() {
    override fun handleAction(action: CourseBenefitsFeature.Action) {
        when (action) {
            is CourseBenefitsFeature.Action.FetchCourseBenefits -> {
                compositeDisposable += courseBenefitsInteractor
                    .getCourseBenefits(action.courseId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsSuccess(it)) },
                        onComplete = { onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsSuccess(emptyList())) },
                        onError = { onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsFailure) }
                    )
            }
        }
    }
}