package org.stepik.android.presentation.vote.transition

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class ChangeVoteTransition
@Inject
constructor(
    private val commentInteractor: CommentInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) {
    operator fun invoke(commentDataItem: CommentItem.Data, voteValue: Vote.Value) {

    }
}