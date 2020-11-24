package org.stepik.android.presentation.step_quiz.dispatcher

import io.reactivex.Scheduler
import io.reactivex.Single
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
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
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
) : RxActionDispatcher<StepQuizFeature.Action, StepQuizFeature.Message>() {
    override fun handleAction(action: StepQuizFeature.Action) {
        when (action) {
            is StepQuizFeature.Action.FetchAttempt ->
                compositeDisposable += stepQuizInteractor
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
                        onSuccess = ::onNewMessage,
                        onError = { onNewMessage(StepQuizFeature.Message.FetchAttemptError) }
                    )

            is StepQuizFeature.Action.CreateAttempt ->
                if (stepQuizInteractor.isNeedRecreateAttemptForNewSubmission(action.step)) {
                    val reply = (action.submissionState as? StepQuizFeature.SubmissionState.Loaded)
                        ?.submission
                        ?.reply

                    compositeDisposable += stepQuizInteractor
                        .createAttempt(action.step.id)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onSuccess = { onNewMessage(StepQuizFeature.Message.CreateAttemptSuccess(it, StepQuizFeature.SubmissionState.Empty(reply = reply))) },
                            onError = { onNewMessage(StepQuizFeature.Message.CreateAttemptError) }
                        )
                } else {
                    val submissionState = (action.submissionState as? StepQuizFeature.SubmissionState.Loaded)
                        ?.submission
                        ?.let { Submission(id = it.id + 1, attempt = action.attempt.id, _reply = it.reply, status = Submission.Status.LOCAL) }
                        ?.let { StepQuizFeature.SubmissionState.Loaded(it) }
                        ?: StepQuizFeature.SubmissionState.Empty()

                    onNewMessage(StepQuizFeature.Message.CreateAttemptSuccess(action.attempt, submissionState))
                }

            is StepQuizFeature.Action.CreateSubmission ->
                compositeDisposable += stepQuizInteractor
                    .createSubmission(action.step.id, action.attemptId, action.reply)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { newSubmission ->
                            onNewMessage(StepQuizFeature.Message.CreateSubmissionSuccess(newSubmission))

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
                        onError = { onNewMessage(StepQuizFeature.Message.CreateSubmissionError) }
                    )

            is StepQuizFeature.Action.SaveLocalSubmission ->
                compositeDisposable += stepQuizInteractor
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
}