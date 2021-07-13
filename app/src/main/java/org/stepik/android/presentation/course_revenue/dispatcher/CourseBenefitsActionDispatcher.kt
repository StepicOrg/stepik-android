package org.stepik.android.presentation.course_revenue.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Maybes.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_revenue.interactor.CourseBenefitsInteractor
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
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
                compositeDisposable += zip(
                    courseBenefitsInteractor.getCourseBenefits(action.courseId),
                    courseBenefitsInteractor.getCourseBeneficiary(action.courseId).toMaybe()
                )
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { (courseBenefits, courseBeneficiary) -> onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsSuccess(courseBenefits, courseBeneficiary)) },
                    onComplete = { onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsSuccess(emptyList(), CourseBeneficiary.EMPTY)) },
                    onError = { onNewMessage(CourseBenefitsFeature.Message.FetchCourseBenefitsFailure) }
                )
            }
        }
    }
}