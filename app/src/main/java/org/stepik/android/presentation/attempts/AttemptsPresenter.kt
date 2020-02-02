package org.stepik.android.presentation.attempts

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.attempts.interactor.AttemptsInteractor
import org.stepik.android.model.Submission
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.attempts.model.AttemptCacheItem
import org.stepik.android.view.injection.attempts.AttemptsBus
import timber.log.Timber
import javax.inject.Inject

class AttemptsPresenter
@Inject
constructor(
    private val attemptsInteractor: AttemptsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @AttemptsBus
    private val attemptsObservable: Observable<Unit>
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
        // TODO Ask about bus
        // subscribeForAttemptsUpdates()
    }

    fun fetchAttemptCacheItems(forceUpdate: Boolean = false) {
        if (state == AttemptsView.State.Idle || forceUpdate) {
            state = AttemptsView.State.Loading
            compositeDisposable += attemptsInteractor
                .fetchAttemptCacheItems()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { attempts ->
                        state = if (attempts.isEmpty()) {
                            AttemptsView.State.Empty
                        } else {
                            AttemptsView.State.AttemptsLoaded(attempts)
                        }
                    },
                    onError = { state = AttemptsView.State.Error; it.printStackTrace() }
                )
        }
    }

    fun setDataToPresenter(items: List<AttemptCacheItem>) {
        state = AttemptsView.State.AttemptsLoaded(items)
    }

    private fun subscribeForAttemptsUpdates() {
        compositeDisposable += attemptsObservable
            .subscribeOn(mainScheduler)
            .observeOn(backgroundScheduler)
            .subscribeBy(
                onNext = {
                    Timber.d("OnNext")
                    fetchAttemptCacheItems(forceUpdate = true) },
                onError = { it.printStackTrace() }
            )
    }

    fun submitSolutions(submissions: List<Submission>) {
        if (state !is AttemptsView.State.AttemptsLoaded) return

        state = AttemptsView.State.AttemptsSending()

        compositeDisposable += attemptsInteractor
            .sendSubmissions(submissions)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    view?.setState(AttemptsView.State.AttemptsSending(it))
                    Timber.d("Next: $it")
                },
                onComplete = {
                    view?.setState(AttemptsView.State.AttemptsSent)
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