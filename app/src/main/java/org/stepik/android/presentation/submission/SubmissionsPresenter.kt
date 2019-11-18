package org.stepik.android.presentation.submission

import org.stepic.droid.util.plus
import org.stepik.android.domain.submission.interactor.SubmissionInteractor
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class SubmissionsPresenter
@Inject
constructor(
    private val submissionInteractor: SubmissionInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<SubmissionsView>() {
    private var state: SubmissionsView.State = SubmissionsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: SubmissionsView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchSubmissions(stepId: Long, forceUpdate: Boolean = false) {
        if (state != SubmissionsView.State.Idle &&
            !((state == SubmissionsView.State.NetworkError || state is SubmissionsView.State.Content) && forceUpdate)) {
            return
        }

        val oldState = state
        compositeDisposable.clear()

        state = SubmissionsView.State.Loading
        compositeDisposable += submissionInteractor
            .getSubmissionItems(stepId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state =
                        if (it.isEmpty()) {
                            SubmissionsView.State.ContentEmpty
                        } else {
                            SubmissionsView.State.Content(it)
                        }
                },
                onError = {
                    if (oldState is SubmissionsView.State.Content) {
                        state = oldState
                        view?.showNetworkError()
                    } else {
                        state = SubmissionsView.State.NetworkError
                    }
                }
            )
    }

    fun fetchNextPage(stepId: Long) {
        val oldState = (state as? SubmissionsView.State.Content)
            ?.takeIf { it.items.hasNext }
            ?: return

        state = SubmissionsView.State.ContentLoading(oldState.items)
        compositeDisposable += submissionInteractor
            .getSubmissionItems(stepId, page = oldState.items.page + 1)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = SubmissionsView.State.Content(oldState.items + it) },
                onError = { state = oldState; view?.showNetworkError() }
            )
    }
}