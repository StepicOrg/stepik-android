package org.stepic.droid.core.presenters.contracts

interface VoteView {

    fun onVoteFail()

    fun onVoteSuccess(commentId: Long)

}
