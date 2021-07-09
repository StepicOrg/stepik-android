package org.stepik.android.presentation.course_benefits.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_benefits.interactor.CourseBenefitsInteractor
import org.stepik.android.presentation.course_benefits.CourseBenefitsPurchasesAndRefundsFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseBenefitsPurchasesAndRefundsActionDispatcher
@Inject
constructor(
    private val courseBenefitsInteractor: CourseBenefitsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseBenefitsPurchasesAndRefundsFeature.Action, CourseBenefitsPurchasesAndRefundsFeature.Message>() {
    override fun handleAction(action: CourseBenefitsPurchasesAndRefundsFeature.Action) {
        when (action) {
            is CourseBenefitsPurchasesAndRefundsFeature.Action.FetchCourseBenefits -> {
                compositeDisposable += courseBenefitsInteractor
                    .getCourseBenefits()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseBenefitsPurchasesAndRefundsFeature.Message.FetchCourseBenefitsSuccess(it)) },
                        onComplete = { onNewMessage(CourseBenefitsPurchasesAndRefundsFeature.Message.FetchCourseBenefitsSuccess(emptyList())) },
                        onError = { onNewMessage(CourseBenefitsPurchasesAndRefundsFeature.Message.FetchCourseBenefitsFailure) }
                    )
            }
        }
    }
}