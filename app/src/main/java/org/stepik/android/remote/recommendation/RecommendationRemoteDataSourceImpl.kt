package org.stepik.android.remote.recommendation

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.recommendation.source.RecommendationRemoteDataSource
import org.stepik.android.model.adaptive.Recommendation
import org.stepik.android.model.adaptive.RecommendationReaction
import org.stepik.android.remote.recommendation.model.RecommendationReactionsRequest
import org.stepik.android.remote.recommendation.model.RecommendationsResponse
import org.stepik.android.remote.recommendation.service.RecommendationService
import javax.inject.Inject

class RecommendationRemoteDataSourceImpl
@Inject
constructor(
    private val recommendationService: RecommendationService
) : RecommendationRemoteDataSource {
    override fun getNextRecommendations(courseId: Long, count: Int): Single<List<Recommendation>> =
        recommendationService.getNextRecommendations(courseId, count).map(RecommendationsResponse::recommendations)

    override fun createReaction(reaction: RecommendationReaction): Completable =
        recommendationService.createRecommendationReaction(RecommendationReactionsRequest(reaction))
}