package org.stepik.android.presentation.comment

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.comment.interactor.ComposeCommentInteractor
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.submission.interactor.LastSubmissionInteractor
import org.stepik.android.model.Submission
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ComposeCommentPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val composeCommentInteractor: ComposeCommentInteractor,
    private val lastSubmissionInteractor: LastSubmissionInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ComposeCommentView>() {
    private var state: ComposeCommentView.State = ComposeCommentView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ComposeCommentView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onData(discussionThread: DiscussionThread, target: Long, parent: Long?, forceUpdate: Boolean = false) {
        if (state != ComposeCommentView.State.Idle &&
            !(state == ComposeCommentView.State.NetworkError && forceUpdate)) {
            return
        }

        if (discussionThread.thread == DiscussionThread.THREAD_SOLUTIONS &&
            parent == null
        ) {
            state = ComposeCommentView.State.Loading
            compositeDisposable += lastSubmissionInteractor
                .getLastSubmission(target)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribeBy(
                    onSuccess = { state = ComposeCommentView.State.Create(it) },
                    onError = { state = ComposeCommentView.State.NetworkError }
                )
        } else {
            state = ComposeCommentView.State.Create(submission = null)
        }
    }

    fun onSubmissionSelected(submission: Submission) {
        if (state is ComposeCommentView.State.Create) {
            state = ComposeCommentView.State.Create(submission)
        }
    }

    fun createComment(comment: Comment) {
        val oldState = (state as? ComposeCommentView.State.Create)
            ?: return

        replaceComment(
            composeCommentInteractor
                .createComment(comment.copy(submission = oldState.submission?.id))
                .doOnSuccess { analytic.reportEvent(Analytic.Comments.COMMENTS_SENT_SUCCESSFULLY) },
            isCommentCreated = true
        )
    }

    fun updateComment(comment: Comment) {
        val oldState = (state as? ComposeCommentView.State.Create)
            ?: return

        replaceComment(composeCommentInteractor.saveComment(comment), isCommentCreated = false)
    }

    private fun replaceComment(commentSource: Single<CommentsData>, isCommentCreated: Boolean) {
        state = ComposeCommentView.State.Loading
        compositeDisposable += commentSource
            .doOnSubscribe { analytic.reportEvent(Analytic.Comments.CLICK_SEND_COMMENTS) }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = ComposeCommentView.State.Complete(it, isCommentCreated) },
                onError = { state = ComposeCommentView.State.Idle; view?.showNetworkError() }
            )
    }
}