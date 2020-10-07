package org.stepik.android.presentation.step_quiz

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.getStepType
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Reply
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.step_quiz.model.StepQuizReducer
import ru.nobird.android.core.model.mapOfNotNull
import ru.nobird.android.domain.rx.emptyOnErrorStub
import java.util.Calendar
import javax.inject.Inject

class StepQuizPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val stepQuizInteractor: StepQuizInteractor,
    private val stepQuizReducer: StepQuizReducer,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizView>() {
    private var state: StepQuizView.State = StepQuizView.State.Idle
        set(value) {
            field = value
            view?.render(value)
        }

    private val viewActionQueue = ArrayDeque<StepQuizView.Action.ViewAction>()

    override fun attachView(view: StepQuizView) {
        super.attachView(view)
        view.render(state)
        while (viewActionQueue.isNotEmpty()) {
            view.onAction(viewActionQueue.removeFirst())
        }
    }

    fun onNewMessage(message: StepQuizView.Message) {
        val (newState, actions) = stepQuizReducer.reduce(state, message)

        state = newState
        actions.forEach(::handleAction)
    }

    private fun handleAction(action: StepQuizView.Action) {
        when (action) {
            is StepQuizView.Action.ViewAction ->
                view?.onAction(action) ?: viewActionQueue.addLast(action)

            is StepQuizView.Action.FetchAttempt ->
                compositeDisposable += stepQuizInteractor
                    .getAttempt(action.stepWrapper.step.id)
                    .flatMap { attempt ->
                        zip(
                            getSubmissionState(attempt.id),
                            stepQuizInteractor.getStepRestrictions(action.stepWrapper, action.lessonData)
                        ) { submissionState, stepRestrictions ->
                            StepQuizView.Message.FetchAttemptSuccess(attempt, submissionState, stepRestrictions)
                        }
                    }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = ::onNewMessage,
                        onError = { onNewMessage(StepQuizView.Message.FetchAttemptError) }
                    )

            is StepQuizView.Action.CreateAttempt ->
                if (stepQuizInteractor.isNeedRecreateAttemptForNewSubmission(action.step)) {
                    val reply = (action.submissionState as? StepQuizView.SubmissionState.Loaded)
                        ?.submission
                        ?.reply

                    compositeDisposable += stepQuizInteractor
                        .createAttempt(action.step.id)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onSuccess = { onNewMessage(StepQuizView.Message.CreateAttemptSuccess(it, StepQuizView.SubmissionState.Empty(reply = reply))) },
                            onError = { onNewMessage(StepQuizView.Message.CreateAttemptError) }
                        )
                } else {
                    val submissionState = (action.submissionState as? StepQuizView.SubmissionState.Loaded)
                        ?.submission
                        ?.let { Submission(id = it.id + 1, attempt = action.attempt.id, _reply = it.reply, status = Submission.Status.LOCAL) }
                        ?.let { StepQuizView.SubmissionState.Loaded(it) }
                        ?: StepQuizView.SubmissionState.Empty()

                    onNewMessage(StepQuizView.Message.CreateAttemptSuccess(action.attempt, submissionState))
                }

            is StepQuizView.Action.CreateSubmission ->
                compositeDisposable += stepQuizInteractor
                    .createSubmission(action.step.id, action.attemptId, action.reply)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { newSubmission ->
                            onNewMessage(StepQuizView.Message.CreateSubmissionSuccess(newSubmission))

                            val params =
                                mapOfNotNull(
                                    AmplitudeAnalytic.Steps.Params.SUBMISSION to newSubmission.id,
                                    AmplitudeAnalytic.Steps.Params.STEP to action.step.id,
                                    AmplitudeAnalytic.Steps.Params.TYPE to action.step.getStepType(),
                                    AmplitudeAnalytic.Steps.Params.LOCAL to false,
                                    AmplitudeAnalytic.Steps.Params.IS_ADAPTIVE to false,
                                    AmplitudeAnalytic.Steps.Params.LANGUAGE to newSubmission.reply?.language
                                )

                            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.SUBMISSION_MADE, params)
                        },
                        onError = { onNewMessage(StepQuizView.Message.CreateSubmissionError) }
                    )

            is StepQuizView.Action.SaveLocalSubmission ->
                compositeDisposable += stepQuizInteractor
                    .createLocalSubmission(action.submission)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(onError = emptyOnErrorStub)
        }
    }

    fun onStepData(stepWrapper: StepPersistentWrapper, lessonData: LessonData, forceUpdate: Boolean = false) {
        if (state == StepQuizView.State.Idle ||
            state == StepQuizView.State.NetworkError && forceUpdate) {
            fetchAttempt(stepWrapper, lessonData)
        }
    }

    private fun fetchAttempt(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        state = StepQuizView.State.Loading
        compositeDisposable += stepQuizInteractor
            .getAttempt(stepWrapper.step.id)
            .flatMap { attempt ->
                zip(
                    getSubmissionState(attempt.id),
                    stepQuizInteractor.getStepRestrictions(stepWrapper, lessonData)
                ) { submissionState, stepRestrictions ->
                    StepQuizView.State.AttemptLoaded(attempt, submissionState, stepRestrictions)
                }
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = it },
                onError = { state = StepQuizView.State.NetworkError }
            )
    }

    private fun getSubmissionState(attemptId: Long): Single<StepQuizView.SubmissionState> =
        stepQuizInteractor
            .getSubmission(attemptId)
            .map<StepQuizView.SubmissionState> { StepQuizView.SubmissionState.Loaded(it) }
            .toSingle(StepQuizView.SubmissionState.Empty())

    /**
     * Attempts
     */
    fun createAttempt(step: Step) {
        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return

        if (stepQuizInteractor.isNeedRecreateAttemptForNewSubmission(step)) {
            state = StepQuizView.State.Loading

            val reply = (oldState.submissionState as? StepQuizView.SubmissionState.Loaded)
                ?.submission
                ?.reply

            compositeDisposable += stepQuizInteractor
                .createAttempt(step.id)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { state = StepQuizView.State.AttemptLoaded(it, StepQuizView.SubmissionState.Empty(reply = reply), oldState.restrictions) },
                    onError = { state = StepQuizView.State.NetworkError }
                )
        } else {
            val submissionState = (oldState.submissionState as? StepQuizView.SubmissionState.Loaded)
                ?.submission
                ?.let { Submission(id = it.id + 1, attempt = oldState.attempt.id, _reply = it.reply, status = Submission.Status.LOCAL) }
                ?.let { StepQuizView.SubmissionState.Loaded(it) }
                ?: StepQuizView.SubmissionState.Empty()

            state = oldState.copy(submissionState = submissionState)
        }
    }

    /**
     * Submissions
     */
    fun createSubmission(step: Step, reply: Reply) {
        syncReplyState(reply)

        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return

        val submission = Submission(attempt = oldState.attempt.id, _reply = reply, status = Submission.Status.EVALUATION)

        state = oldState.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission))
        compositeDisposable += stepQuizInteractor
            .createSubmission(oldState.attempt.step, oldState.attempt.id, reply)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { newSubmission ->
                    state = oldState
                        .copy(
                            submissionState = StepQuizView.SubmissionState.Loaded(newSubmission),
                            restrictions = oldState.restrictions.copy(submissionCount = oldState.restrictions.submissionCount + 1)
                        )

                    val params =
                        mapOfNotNull(
                            AmplitudeAnalytic.Steps.Params.SUBMISSION to newSubmission.id,
                            AmplitudeAnalytic.Steps.Params.STEP to step.id,
                            AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                            AmplitudeAnalytic.Steps.Params.LOCAL to false,
                            AmplitudeAnalytic.Steps.Params.IS_ADAPTIVE to false,
                            AmplitudeAnalytic.Steps.Params.LANGUAGE to newSubmission.reply?.language
                        )

                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.SUBMISSION_MADE, params)
                },
                onError = { state = oldState }
            )
    }

    fun syncReplyState(reply: Reply) {
        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return

        val submissionId = (oldState.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission
            ?.id
            ?: 0

        val submission = Submission(id = submissionId, attempt = oldState.attempt.id, _reply = reply, status = Submission.Status.LOCAL, time = Calendar.getInstance().time)
        state = oldState.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission))

        compositeDisposable += stepQuizInteractor
            .createLocalSubmission(submission)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onError = emptyOnErrorStub)
    }
}