package org.stepik.android.presentation.submission

import ru.nobird.app.core.model.plus
import org.stepik.android.domain.submission.interactor.SubmissionInteractor
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
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
    private var state: SubmissionsView.State.Data = SubmissionsView.State.Data(
        submissionsFilterQuery = SubmissionsFilterQuery.DEFAULT_QUERY,
        SubmissionsView.ContentState.Idle
    )
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: SubmissionsView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchSubmissions(
        stepId: Long,
        isTeacher: Boolean,
        submissionsFilterQuery: SubmissionsFilterQuery,
        reviewInstruction: ReviewInstruction? = null,
        forceUpdate: Boolean = false
    ) {
        if (state.contentState != SubmissionsView.ContentState.Idle &&
            !((state.contentState == SubmissionsView.ContentState.NetworkError || state.contentState is SubmissionsView.ContentState.Content || state.contentState is SubmissionsView.ContentState.ContentEmpty) && forceUpdate)) {
            return
        }

        val oldState = state
        compositeDisposable.clear()

        state = state.copy(
            submissionsFilterQuery = submissionsFilterQuery,
            contentState = SubmissionsView.ContentState.Loading
        )
        compositeDisposable += submissionInteractor
            .getSubmissionItems(stepId, isTeacher, state.submissionsFilterQuery, reviewInstruction)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    val newState = if (it.isEmpty()) {
                        SubmissionsView.ContentState.ContentEmpty
                    } else {
                        SubmissionsView.ContentState.Content(it)
                    }
                    state = state.copy(contentState = newState)
                },
                onError = {
                    if (oldState.contentState is SubmissionsView.ContentState.Content) {
                        state = oldState
                        view?.showNetworkError()
                    } else {
                        state = state.copy(contentState = SubmissionsView.ContentState.NetworkError)
                    }
                }
            )
    }

    fun fetchNextPage(stepId: Long, isTeacher: Boolean, reviewInstruction: ReviewInstruction? = null) {
        val oldState = (state.contentState as? SubmissionsView.ContentState.Content)
            ?.takeIf { it.items.hasNext }
            ?: return

        state = state.copy(contentState = SubmissionsView.ContentState.ContentLoading(oldState.items))
        compositeDisposable += submissionInteractor
            .getSubmissionItems(stepId, isTeacher, state.submissionsFilterQuery, reviewInstruction, oldState.items.page + 1)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = state.copy(contentState = SubmissionsView.ContentState.Content(oldState.items + it)) },
                onError = { state = state.copy(contentState = oldState); view?.showNetworkError() }
            )
    }

    fun onFilterMenuItemClicked() {
        view?.showSubmissionsFilterDialog(state.submissionsFilterQuery)
    }
}