package org.stepik.android.presentation.attempts

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.attempts.interactor.AttemptsInteractor
import org.stepik.android.model.Submission
import org.stepik.android.presentation.attempts.mapper.AttemptsStateMapper
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.attempts.AttemptsBus
import timber.log.Timber
import javax.inject.Inject

class AttemptsPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,
    private val attemptsInteractor: AttemptsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @AttemptsBus
    private val attemptsObservable: Observable<Unit>,
    private val attemptsStateMapper: AttemptsStateMapper
) : PresenterBase<AttemptsView>() {
    private var state: AttemptsView.State = AttemptsView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    private var isBlockingLoading = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    override fun attachView(view: AttemptsView) {
        super.attachView(view)
        view.setState(state)
    }

    init {
        subscribeForAttemptsUpdates()
    }

    fun fetchAttemptCacheItems(localOnly: Boolean = true) {
        if (state == AttemptsView.State.Idle || state is AttemptsView.State.AttemptsLoaded) {
            state = if (state !is AttemptsView.State.AttemptsLoaded) {
                AttemptsView.State.Loading
            } else {
                state
            }
            compositeDisposable += attemptsInteractor
                .fetchAttemptCacheItems(courseId, localOnly)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { attempts ->
                        state = if (attempts.isEmpty()) {
                            AttemptsView.State.Empty
                        } else {
                            if (state is AttemptsView.State.AttemptsLoaded) {
                                attemptsStateMapper.mergeStateWithAttemptItems(state, attempts)
                            } else {
                                AttemptsView.State.AttemptsLoaded(attempts, isSending = false)
                            }
                        }
                    },
                    onError = { state = AttemptsView.State.Error; it.printStackTrace() }
                )
        }
    }

    private fun subscribeForAttemptsUpdates() {
        compositeDisposable += attemptsObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    Timber.d("OnNext")
                    fetchAttemptCacheItems(localOnly = false) },
                onError = { it.printStackTrace() }
            )
    }

    fun submitSolutions(submissions: List<Submission>) {
        if (state !is AttemptsView.State.AttemptsLoaded) return

        state = attemptsStateMapper.setItemsEnabled(state, isEnabled = false)

        compositeDisposable += attemptsInteractor
            .sendSubmissions(submissions)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    Timber.d("Next: $it")
                    state = attemptsStateMapper.mergeStateWithSubmission(state, it)
                },
                onComplete = {
                    state = attemptsStateMapper.setItemsEnabled(state, isEnabled = true)
                    view?.onFinishedSending()
                },
                onError = { it.printStackTrace(); Timber.d("Error: $it") }
            )
    }

    fun removeAttempts(attemptIds: List<Long>) {
        isBlockingLoading = true
        compositeDisposable += attemptsInteractor
            .removeAttempts(attemptIds)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onComplete = { state = AttemptsView.State.Idle; fetchAttemptCacheItems() },
                onError = { it.printStackTrace() }
            )
    }
}