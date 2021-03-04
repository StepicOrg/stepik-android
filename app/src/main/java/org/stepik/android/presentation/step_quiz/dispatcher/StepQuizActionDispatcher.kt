package org.stepik.android.presentation.step_quiz.dispatcher

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.getStepType
import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.domain.code_preference.interactor.CodePreferenceInteractor
import org.stepik.android.domain.code_preference.model.InitCodePreference
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.injection.step_quiz.CodePreferenceBus
import ru.nobird.android.core.model.mapOfNotNull
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StepQuizActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val stepQuizInteractor: StepQuizInteractor,
    private val codePreferenceInteractor: CodePreferenceInteractor,

    @CodePreferenceBus
    private val codePreferenceObservable: Observable<InitCodePreference>,
    @CodePreferenceBus
    private val codePreferencePublisher: PublishSubject<InitCodePreference>,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<StepQuizFeature.Action, StepQuizFeature.Message>() {
    init {
        compositeDisposable += codePreferenceObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { onNewMessage(StepQuizFeature.Message.InitWithCodePreference(it)) },
                onError = emptyOnErrorStub
            )
    }
    override fun handleAction(action: StepQuizFeature.Action) {
        when (action) {
            is StepQuizFeature.Action.FetchAttempt ->
                compositeDisposable += stepQuizInteractor
                    .getAttempt(action.stepWrapper.step.id)
                    .flatMap { attempt ->
                        Singles.zip(
                            getSubmissionState(attempt.id),
                            stepQuizInteractor.getStepRestrictions(action.stepWrapper, action.lessonData),
                            resolveCodeQuizLanguage(action.stepWrapper)
                        ) { submissionState, stepRestrictions, codePreference ->
                            val updatedSubmissionState = if (codePreference != CodePreference.EMPTY) {
                                resolveSubmissionState(submissionState, action.stepWrapper, codePreference)
                            } else {
                                submissionState
                            }
                            StepQuizFeature.Message.FetchAttemptSuccess(
                                attempt,
                                updatedSubmissionState,
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

            is StepQuizFeature.Action.SaveCodePreference ->
                compositeDisposable += codePreferenceInteractor
                    .saveCodePreference(action.codePreference)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(onError = emptyOnErrorStub)

            is StepQuizFeature.Action.PublishCodePreference ->
                codePreferencePublisher.onNext(action.initCodePreference)
        }
    }

    fun getSubmissionState(attemptId: Long): Single<StepQuizFeature.SubmissionState> =
        stepQuizInteractor
            .getSubmission(attemptId)
            .map<StepQuizFeature.SubmissionState> { StepQuizFeature.SubmissionState.Loaded(it) }
            .toSingle(StepQuizFeature.SubmissionState.Empty())

    private fun resolveSubmissionState(submissionState: StepQuizFeature.SubmissionState, stepWrapper: StepPersistentWrapper, codePreference: CodePreference): StepQuizFeature.SubmissionState =
        when (submissionState) {
            is StepQuizFeature.SubmissionState.Empty ->
                StepQuizFeature.SubmissionState.Empty(
                    Reply(
                        language = codePreference.preferredLanguage,
                        code = stepWrapper.step.block?.options?.codeTemplates?.get(codePreference.preferredLanguage)
                    )
                )

            is StepQuizFeature.SubmissionState.Loaded -> {
                val codeFromSubmission = submissionState.submission.reply?.code
                val codeTemplate = stepWrapper.step.block?.options?.codeTemplates?.get(submissionState.submission.reply?.language)
                if (codeFromSubmission == codeTemplate) {
                    submissionState.copy(
                        submission = submissionState.submission.copy(
                            _reply = submissionState.submission._reply?.copy(
                                language = codePreference.preferredLanguage,
                                code = stepWrapper.step.block?.options?.codeTemplates?.get(codePreference.preferredLanguage)
                            )
                        )
                    )
                } else {
                    submissionState
                }
            }
        }

    private fun resolveCodeQuizLanguage(stepWrapper: StepPersistentWrapper): Single<CodePreference> =
        if (stepWrapper.step.block?.name == AppConstants.TYPE_CODE) {
            stepWrapper.step.block?.options?.codeTemplates?.entries?.let {
                codePreferenceInteractor.getCodePreference(stepWrapper.step.block?.options?.codeTemplates?.keys?.sorted().toString())
            } ?: Single.just(CodePreference.EMPTY)
        } else {
            Single.just(CodePreference.EMPTY)
        }
}