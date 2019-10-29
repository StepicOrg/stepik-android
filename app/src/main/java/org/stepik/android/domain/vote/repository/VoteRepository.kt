package org.stepik.android.domain.vote.repository

import io.reactivex.Single
import org.stepik.android.model.comments.Vote

interface VoteRepository {
    fun saveVote(vote: Vote): Single<Vote>
}