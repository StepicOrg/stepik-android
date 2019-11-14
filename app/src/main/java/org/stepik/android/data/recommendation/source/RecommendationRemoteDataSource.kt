package org.stepik.android.data.recommendation.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.adaptive.RecommendationReaction
import org.stepik.android.remote.recommendation.model.RecommendationsResponse

interface RecommendationRemoteDataSource {
    fun getNextRecommendations(courseId: Long, count: Int): Single<RecommendationsResponse>

    fun createReaction(reaction: RecommendationReaction): Completable
}