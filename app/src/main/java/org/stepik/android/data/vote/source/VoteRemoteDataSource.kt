package org.stepik.android.data.vote.source

import io.reactivex.Single
import org.stepik.android.model.comments.Vote

interface VoteRemoteDataSource {
    fun saveVote(vote: Vote): Single<Vote>
}