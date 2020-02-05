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
    private val attemptsObservable: Observable<Unit>,
    @SolutionsSentBus
    private val attemptsSentPublisher: PublishSubject<Unit>,
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
        if (state == SolutionsView.State.Idle || state is SolutionsView.State.AttemptsLoaded) {
            state = if (state !is SolutionsView.State.AttemptsLoaded) {
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
                            if (state is SolutionsView.State.AttemptsLoaded) {
                                solutionsStateMapper.mergeStateWithSolutionItems(state, solutions)
                            } else {
                                solutionsStateMapper.mapToSolutionsState(solutions)
                            }
                    },
                    onError = { state = SolutionsView.State.Error; it.printStackTrace() }
                )
        }
    }

    fun fetchSolutionItemsForceUpdate() {
        if (state !is SolutionsView.State.Error) {
            return
        }
        compositeDisposable += solutionsInteractor
            .fetchAttemptCacheItems(courseId, false)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { attempts -> solutionsStateMapper.mapToSolutionsState(attempts) },
                onError = { state = SolutionsView.State.Error; it.printStackTrace() }
            )
    }

    private fun subscribeForSolutionsUpdates() {
        compositeDisposable += attemptsObservable
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
        if (state !is SolutionsView.State.AttemptsLoaded) return

        state = solutionsStateMapper.setSolutionItemsEnabled(state, isEnabled = false)

        sendSubmissionEvents(submissionItems)
        val submissions = submissionItems.map { it.submission }
        compositeDisposable += solutionsInteractor
            .sendSubmissions(submissions)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { state = solutionsStateMapper.mergeStateWithSubmission(state, it) },
                onComplete = {
                    state = solutionsStateMapper.setSolutionItemsEnabled(state, isEnabled = true)
                    attemptsSentPublisher.onNext(Unit)
                    view?.onFinishedSending()
                },
                onError = {
                    val oldState =
                        (state as? SolutionsView.State.AttemptsLoaded)
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
                    attemptsSentPublisher.onNext(Unit)
                },
                onError = { it.printStackTrace() }
            )
    }

    private fun sendSubmissionEvents(submissionItems: List<SolutionItem.SubmissionItem>) {
        submissionItems.forEach { submissionItem ->
            val step = submissionItem.step

            val params =
                mutableMapOf(
                    AmplitudeAnalytic.LocalSubmissions.Params.STEP to step.id,
                    AmplitudeAnalytic.LocalSubmissions.Params.TYPE to step.getStepType()
                )

            submissionItem.submission.reply?.language
                ?.let { lang ->
                    params[AmplitudeAnalytic.LocalSubmissions.Params.LANGUAGE] = lang
                }
            analytic.reportAmplitudeEvent(
                AmplitudeAnalytic.LocalSubmissions.LOCAL_SUBMISSION_MADE,
                params
            )
        }
    }
}