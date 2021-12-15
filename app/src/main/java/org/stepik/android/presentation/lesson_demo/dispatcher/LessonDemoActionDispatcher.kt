package org.stepik.android.presentation.lesson_demo.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.lesson_demo.interactor.LessonDemoInteractor
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class LessonDemoActionDispatcher
@Inject
constructor(
    private val lessonDemoInteractor: LessonDemoInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<LessonDemoFeature.Action, LessonDemoFeature.Message>() {
    override fun handleAction(action: LessonDemoFeature.Action) {
        when (action) {
            is LessonDemoFeature.Action.FetchLessonDemoData -> {
                compositeDisposable += lessonDemoInteractor
                    .getLessonDemoData(action.course)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(LessonDemoFeature.Message.FetchLessonDemoDataSuccess(it.deeplinkPromoCode, it.coursePurchaseDataResult)) },
                        onError = { onNewMessage(LessonDemoFeature.Message.FetchLessonDemoDataFailure) }
                    )
            }
        }
    }
}