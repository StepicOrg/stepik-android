package org.stepik.android.remote.recommendation.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.web.model.adaptive.RecommendationReactionsRequest
import org.stepic.droid.web.model.adaptive.RecommendationsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RecommendationService {
    @GET("api/recommendations")
    fun getNextRecommendations(
        @Query("course") courseId: Long,
        @Query("count") count: Int
    ): Single<RecommendationsResponse>

    @POST("api/recommendation-reactions")
    fun createRecommendationReaction(
        @Body reactionsRequest: RecommendationReactionsRequest
    ): Completable
}