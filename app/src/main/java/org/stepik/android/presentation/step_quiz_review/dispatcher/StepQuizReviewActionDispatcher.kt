package org.stepik.android.presentation.step_quiz_review.dispatcher

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.domain.step_quiz_review.interactor.StepQuizReviewInteractor
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.dispatcher.StepQuizActionDispatcher
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewFeature
import ru.nobird.android.presentation.redux.dispatcher.ActionDispatcher
import javax.inject.Inject

class StepQuizReviewActionDispatcher
@Inject
constructor(
    stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,
    lessonData: LessonData,

    private val stepQuizActionDispatcher: StepQuizActionDispatcher, // todo remove
    private val stepQuizInteractor: StepQuizInteractor,
    private val stepQuizReviewInteractor: StepQuizReviewInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : ActionDispatcher<StepQuizReviewFeature.Action, StepQuizReviewFeature.Message> {
    private val compositeDisposable = CompositeDisposable()
    private var messageListener: ((StepQuizReviewFeature.Message) -> Unit)? = null

    init {
        compositeDisposable += stepWrapperRxRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onNext = { messageListener?.invoke(StepQuizReviewFeature.Message.InitWithStep(it, lessonData)) })
    }

    override fun setListener(listener: (message: StepQuizReviewFeature.Message) -> Unit) {
        messageListener = listener
    }

    override fun handleAction(action: StepQuizReviewFeature.Action) {
        when (action) {
            is StepQuizReviewFeature.Action.FetchStepQuizState -> {
                compositeDisposable += Singles
                    .zip(
                        getAttemptState(action.stepWrapper, action.lessonData),
                        stepQuizReviewInteractor.getStepProgress(action.stepWrapper.step.id, action.lessonData.unit?.id)
                    )
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { (quizState, progress) ->
                            messageListener?.invoke(StepQuizReviewFeature.Message.FetchStepQuizStateSuccess(quizState, progress.firstOrNull()))
                        },
                        onError = { messageListener?.invoke(StepQuizReviewFeature.Message.InitialFetchError) }
                    )
            }

            is StepQuizReviewFeature.Action.FetchReviewSession -> {
                compositeDisposable += stepQuizReviewInteractor
                    .getReviewSession(action.stepId, action.unitId, action.instructionId, action.sessionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { (instruction, sessionData, progress) ->
                            messageListener?.invoke(StepQuizReviewFeature.Message.FetchReviewSessionSuccess(sessionData, instruction, progress.firstOrNull()))
                        },
                        onError = { messageListener?.invoke(StepQuizReviewFeature.Message.InitialFetchError) }
                    )
            }

            is StepQuizReviewFeature.Action.CreateSessionWithSubmission -> {
                compositeDisposable += stepQuizReviewInteractor
                    .createSession(action.submissionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { (session, instruction) ->
                            messageListener?.invoke(StepQuizReviewFeature.Message.SessionCreated(session, instruction))
                        },
                        onError = { messageListener?.invoke(StepQuizReviewFeature.Message.CreateSessionError) }
                    )
            }

            is StepQuizReviewFeature.Action.CreateReviewWithSession -> {
                compositeDisposable += stepQuizReviewInteractor
                    .createReview(action.sessionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { review ->
                            messageListener?.invoke(StepQuizReviewFeature.Message.ReviewCreated(review.id))
                        },
                        onError = { messageListener?.invoke(StepQuizReviewFeature.Message.StartReviewError) }
                    )
            }
        }
    }

    private fun getAttemptState(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Single<StepQuizFeature.State.AttemptLoaded> =
        stepQuizInteractor
            .getAttempt(stepWrapper.id)
            .flatMap { attempt ->
                Singles
                    .zip(
                        stepQuizActionDispatcher.getSubmissionState(attempt.id),
                        stepQuizInteractor.getStepRestrictions(stepWrapper, lessonData)
                    )
                    .map { (submissionState, stepRestrictions) ->
                        StepQuizFeature.State.AttemptLoaded(attempt, submissionState, stepRestrictions)
                    }
            }

    override fun cancel() {
        compositeDisposable.clear()
    }
}