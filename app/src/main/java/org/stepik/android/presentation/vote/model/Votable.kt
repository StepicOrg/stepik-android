package org.stepik.android.presentation.vote.model

interface Votable<T> {
    val voteStatus: VoteStatus

    fun mutate(voteStatus: VoteStatus): T
}