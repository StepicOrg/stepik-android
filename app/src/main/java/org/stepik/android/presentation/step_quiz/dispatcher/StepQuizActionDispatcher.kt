package org.stepik.android.presentation.step_quiz.dispatcher

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.getStepType
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import ru.nobird.android.core.model.mapOfNotNull
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.ActionDispatcher
import javax.inject.Inject

class StepQuizActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val stepQuizInteractor: StepQuizInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : ActionDispatcher<StepQuizFeature.Action, StepQuizFeature.Message> {
    private val disposable = CompositeDisposable()
    private var messageListener: ((StepQuizFeature.Message) -> Unit)? = null

    override fun setListener(listener: (message: StepQuizFeature.Message) -> Unit) {
        messageListener = listener
    }

    override fun handleAction(action: StepQuizFeature.Action) {
        when (action) {
            is StepQuizFeature.Action.FetchAttempt ->
                disposable += stepQuizInteractor
                    .getAttempt(action.stepWrapper.step.id)
                    .flatMap { attempt ->
                        Singles.zip(
                            getSubmissionState(attempt.id),
                            stepQuizInteractor.getStepRestrictions(action.stepWrapper, action.lessonData)
                        ) { submissionState, stepRestrictions ->
                            StepQuizFeature.Message.FetchAttemptSuccess(
                                attempt,
                                submissionState,
                                stepRestrictions
                            )
                        }
                    }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { messageListener?.invoke(it) },
                        onError = { messageListener?.invoke(StepQuizFeature.Message.FetchAttemptError) }
                    )

            is StepQuizFeature.Action.CreateAttempt ->
                if (stepQuizInteractor.isNeedRecreateAttemptForNewSubmission(action.step)) {
                    val reply = (action.submissionState as? StepQuizFeature.SubmissionState.Loaded)
                        ?.submission
                        ?.reply

                    disposable += stepQuizInteractor
                        .createAttempt(action.step.id)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onSuccess = { messageListener?.invoke(StepQuizFeature.Message.CreateAttemptSuccess(it, StepQuizFeature.SubmissionState.Empty(reply = reply))) },
                            onError = { messageListener?.invoke(StepQuizFeature.Message.CreateAttemptError) }
                        )
                } else {
                    val submissionState = (action.submissionState as? StepQuizFeature.SubmissionState.Loaded)
                        ?.submission
                        ?.let { Submission(id = it.id + 1, attempt = action.attempt.id, _reply = it.reply, status = Submission.Status.LOCAL) }
                        ?.let { StepQuizFeature.SubmissionState.Loaded(it) }
                        ?: StepQuizFeature.SubmissionState.Empty()

                    messageListener?.invoke(StepQuizFeature.Message.CreateAttemptSuccess(action.attempt, submissionState))
                }

            is StepQuizFeature.Action.CreateSubmission ->
                disposable += stepQuizInteractor
                    .createSubmission(action.step.id, action.attemptId, action.reply)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { newSubmission ->
                            messageListener?.invoke(StepQuizFeature.Message.CreateSubmissionSuccess(newSubmission))

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
                        onError = { messageListener?.invoke(StepQuizFeature.Message.CreateSubmissionError) }
                    )

            is StepQuizFeature.Action.SaveLocalSubmission ->
                disposable += stepQuizInteractor
                    .createLocalSubmission(action.submission)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(onError = emptyOnErrorStub)
        }
    }

    fun getSubmissionState(attemptId: Long): Single<StepQuizFeature.SubmissionState> =
        stepQuizInteractor
            .getSubmission(attemptId)
            .map<StepQuizFeature.SubmissionState> { StepQuizFeature.SubmissionState.Loaded(it) }
            .toSingle(StepQuizFeature.SubmissionState.Empty())

    override fun cancel() {
        disposable.clear()
    }
}