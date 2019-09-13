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
import org.stepik.android.model.comments.Comment
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ComposeCommentPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val composeCommentInteractor: ComposeCommentInteractor,

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

    fun createComment(comment: Comment) {
        replaceComment(
            composeCommentInteractor
                .createComment(comment)
                .doOnSuccess { analytic.reportEvent(Analytic.Comments.COMMENTS_SENT_SUCCESSFULLY) },
            isCommentCreated = true
        )
    }

    fun updateComment(comment: Comment) {
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