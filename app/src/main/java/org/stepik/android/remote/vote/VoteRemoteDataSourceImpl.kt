package org.stepik.android.remote.vote

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.vote.source.VoteRemoteDataSource
import org.stepik.android.model.comments.Vote
import org.stepik.android.remote.vote.model.VoteRequest
import org.stepik.android.remote.vote.model.VoteResponse
import org.stepik.android.remote.vote.service.VoteService
import javax.inject.Inject

class VoteRemoteDataSourceImpl
@Inject
constructor(
    private val voteService: VoteService
) : VoteRemoteDataSource {
    private val voteResponseMapper =
        Function { response: VoteResponse -> response.votes.first() }

    override fun saveVote(vote: Vote): Single<Vote> =
        voteService
            .saveVote(vote.id, VoteRequest(vote))
            .map(voteResponseMapper)
}