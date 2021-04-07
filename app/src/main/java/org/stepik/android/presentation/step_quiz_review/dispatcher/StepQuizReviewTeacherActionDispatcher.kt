package org.stepik.android.presentation.step_quiz_review.dispatcher

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz_review.interactor.StepQuizReviewInteractor
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StepQuizReviewTeacherActionDispatcher
@Inject
constructor(
    stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,
    lessonData: LessonData,

    private val stepQuizReviewInteractor: StepQuizReviewInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<StepQuizReviewTeacherFeature.Action, StepQuizReviewTeacherFeature.Message>() {
    init {
        compositeDisposable += stepWrapperRxRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onNext = {
                val instructionType = it.step.instructionType ?: return@subscribeBy
                onNewMessage(StepQuizReviewTeacherFeature.Message.InitWithStep(it, lessonData, instructionType))
            })
    }

    override fun handleAction(action: StepQuizReviewTeacherFeature.Action) {
        when (action) {
            is StepQuizReviewTeacherFeature.Action.FetchData -> {
                val sessionId = action.stepWrapper.step.session
                if (sessionId != null && action.instructionType == ReviewStrategyType.INSTRUCTOR) {
                    compositeDisposable += stepQuizReviewInteractor
                        .getReviewSession(sessionId)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onSuccess = { session ->
                                onNewMessage(StepQuizReviewTeacherFeature.Message.FetchDataSuccess(action.instructionType, session))
                            },
                            onError = {
                                onNewMessage(StepQuizReviewTeacherFeature.Message.FetchDataError)
                            }
                        )
                } else {
                    onNewMessage(StepQuizReviewTeacherFeature.Message.FetchDataSuccess(action.instructionType, reviewSession = null))
                }
            }
        }
    }
}