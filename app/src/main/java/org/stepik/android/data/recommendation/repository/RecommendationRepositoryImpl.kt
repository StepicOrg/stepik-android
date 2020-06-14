package org.stepik.android.data.recommendation.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.recommendation.source.RecommendationRemoteDataSource
import org.stepik.android.domain.recommendation.repository.RecommendationRepository
import org.stepik.android.model.adaptive.Recommendation
import org.stepik.android.model.adaptive.RecommendationReaction
import javax.inject.Inject

class RecommendationRepositoryImpl
@Inject
constructor(
    private val recommendationRemoteDataSource: RecommendationRemoteDataSource
) : RecommendationRepository {
    override fun getNextRecommendations(courseId: Long, count: Int): Single<List<Recommendation>> =
        recommendationRemoteDataSource.getNextRecommendations(courseId, count)

    override fun createReaction(reaction: RecommendationReaction): Completable =
        recommendationRemoteDataSource.createReaction(reaction)
}