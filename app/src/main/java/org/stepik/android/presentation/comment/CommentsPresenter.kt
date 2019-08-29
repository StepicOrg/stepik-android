package org.stepik.android.presentation.comment

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.domain.discussion_proxy.interactor.DiscussionProxyInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CommentsPresenter
@Inject
constructor(
    private val commentInteractor: CommentInteractor,
    private val discussionProxyInteractor: DiscussionProxyInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CommentsView>() {
    private var state: CommentsView.State = CommentsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }


}