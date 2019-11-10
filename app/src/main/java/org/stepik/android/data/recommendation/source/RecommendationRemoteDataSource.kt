package org.stepik.android.data.recommendation.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.web.model.adaptive.RecommendationsResponse
import org.stepik.android.model.adaptive.RecommendationReaction

interface RecommendationRemoteDataSource {
    fun getNextRecommendations(courseId: Long, count: Int): Single<RecommendationsResponse>

    fun createReaction(reaction: RecommendationReaction): Completable
}