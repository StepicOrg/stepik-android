package org.stepik.android.data.vote.repository

import io.reactivex.Single
import org.stepik.android.data.vote.source.VoteRemoteDataSource
import org.stepik.android.domain.vote.repository.VoteRepository
import org.stepik.android.model.comments.Vote
import javax.inject.Inject

class VoteRepositoryImpl
@Inject
constructor(
    private val voteRemoteDataSource: VoteRemoteDataSource
) : VoteRepository {
    override fun saveVote(vote: Vote): Single<Vote> =
        voteRemoteDataSource
            .saveVote(vote)
}