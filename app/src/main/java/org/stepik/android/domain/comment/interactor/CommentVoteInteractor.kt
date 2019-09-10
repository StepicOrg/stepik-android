package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
import org.stepik.android.domain.vote.repository.VoteRepository
import org.stepik.android.model.comments.Vote
import javax.inject.Inject

class CommentVoteInteractor
@Inject
constructor(
    private val voteRepository: VoteRepository
) {
    fun changeCommentVote(vote: Vote): Single<Vote> =
        voteRepository
            .saveVote(vote)
}