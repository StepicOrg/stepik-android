package org.stepik.android.data.recommendation.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.adaptive.Recommendation
import org.stepik.android.model.adaptive.RecommendationReaction

interface RecommendationRemoteDataSource {
    fun getNextRecommendations(courseId: Long, count: Int): Single<List<Recommendation>>
    fun createReaction(reaction: RecommendationReaction): Completable
}