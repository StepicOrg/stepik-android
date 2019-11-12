package org.stepik.android.remote.vote.service

import io.reactivex.Single
import org.stepik.android.remote.vote.model.VoteRequest
import org.stepik.android.remote.vote.model.VoteResponse
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface VoteService {
    @PUT("api/votes/{id}")
    abstract fun saveVote(@Path("id") voteId: String, @Body voteRequest: VoteRequest): Single<VoteResponse>
}