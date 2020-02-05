package org.stepik.android.presentation.solutions

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.getStepType
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.solutions.mapper.SolutionsStateMapper
import org.stepik.android.view.injection.solutions.SolutionsBus
import org.stepik.android.view.injection.solutions.SolutionsSentBus
import javax.inject.Inject

class SolutionsPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,
    private val analytic: Analytic,
    private val solutionsInteractor: SolutionsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @SolutionsBus
    private val solutionsObservable: Observable<Unit>,
    @SolutionsSentBus
    private val solutionsSent: PublishSubject<Unit>,
    private val solutionsStateMapper: SolutionsStateMapper
) : PresenterBase<SolutionsView>() {
    private var state: SolutionsView.State = SolutionsView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    private var isBlockingLoading = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    override fun attachView(view: SolutionsView) {
        super.attachView(view)
        view.setState(state)
    }
    init {
        subscribeForSolutionsUpdates()
    }

    fun fetchSolutionItems(localOnly: Boolean = true) {
        if (state == SolutionsView.State.Idle ||
            state is SolutionsView.State.SolutionsLoaded ||
            state == SolutionsView.State.Error
        ) {
            state = if (state !is SolutionsView.State.SolutionsLoaded) {
                SolutionsView.State.Loading
            } else {
                state
            }
            compositeDisposable += solutionsInteractor
                .fetchAttemptCacheItems(courseId, localOnly)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { solutions ->
                        state =
                            if (state is SolutionsView.State.SolutionsLoaded) {
                                solutionsStateMapper.mergeStateWithSolutionItems(state, solutions)
                            } else {
                                solutionsStateMapper.mapToSolutionsState(solutions)
                            }
                    },
                    onError = { state = SolutionsView.State.Error }
                )
        }
    }

    private fun subscribeForSolutionsUpdates() {
        compositeDisposable += solutionsObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    if (state is SolutionsView.State.Empty) {
                        state = SolutionsView.State.Idle
                        fetchSolutionItems(localOnly = true)
                    } else {
                        fetchSolutionItems(localOnly = false)
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    fun submitSolutions(submissionItems: List<SolutionItem.SubmissionItem>) {
        if (state !is SolutionsView.State.SolutionsLoaded) return

        state = solutionsStateMapper.setSolutionItemsEnabled(state, isEnabled = false)

        compositeDisposable += solutionsInteractor
            .sendSubmissions(submissionItems)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { (step, submission) ->
                    sendSubmissionEvent(step, submission)
                    state = solutionsStateMapper.mergeStateWithSubmission(state, submission)
                },
                onComplete = {
                    state = solutionsStateMapper.setSolutionItemsEnabled(state, isEnabled = true)
                    solutionsSent.onNext(Unit)
                    view?.onFinishedSending()
                },
                onError = {
                    val oldState =
                        (state as? SolutionsView.State.SolutionsLoaded)
                        ?: return@subscribeBy
                    state = solutionsStateMapper.setSolutionItemsEnabled(
                        oldState.copy(isSending = false),
                        isEnabled = true
                    )
                    view?.showNetworkError()
                }
            )
    }

    fun removeSolutions(attemptIds: List<Long>) {
        isBlockingLoading = true
        compositeDisposable += solutionsInteractor
            .removeAttempts(attemptIds)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onComplete = {
                    state = SolutionsView.State.Idle
                    fetchSolutionItems(localOnly = false)
                    solutionsSent.onNext(Unit)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun sendSubmissionEvent(step: Step, submission: Submission) {
        val params =
            mutableMapOf(
                AmplitudeAnalytic.Steps.Params.STEP to step.id,
                AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                AmplitudeAnalytic.Steps.Params.LOCAL to true
            )

        submission.reply?.language
            ?.let { lang ->
                params[AmplitudeAnalytic.Steps.Params.LANGUAGE] = lang
            }
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Steps.SUBMISSION_MADE,
            params
        )
    }
}