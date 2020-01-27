package org.stepik.android.presentation.attempts

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.attempts.interactor.AttemptsInteractor
import org.stepik.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject

class AttemptsPresenter
@Inject
constructor(
    private val attemptsInteractor: AttemptsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
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

    fun fetchAttemptCacheItems() {
        if (state != AttemptsView.State.Idle) return

        state = AttemptsView.State.Loading
        compositeDisposable += attemptsInteractor
            .fetchAttemptCacheItems()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { attempts ->
                    Timber.d("Attempts: $attempts")
                    state = if (attempts.isEmpty()) {
                        AttemptsView.State.Empty
                    } else {
                        AttemptsView.State.AttemptsLoaded(attempts)
                    }
                },
                onError = { state = AttemptsView.State.Error; it.printStackTrace() }
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