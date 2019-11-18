package org.stepik.android.domain.recommendation.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.adaptive.Recommendation
import org.stepik.android.model.adaptive.RecommendationReaction

interface RecommendationRepository {
    fun getNextRecommendations(courseId: Long, count: Int): Single<List<Recommendation>>
    fun createReaction(reaction: RecommendationReaction): Completable
}