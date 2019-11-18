package org.stepik.android.data.rating.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.rating.source.RatingRemoteDataSource
import org.stepik.android.domain.rating.repository.RatingRepository
import org.stepik.android.model.adaptive.RatingItem
import javax.inject.Inject

class RatingRepositoryImpl
@Inject
constructor(
    private val ratingRemoteDataSource: RatingRemoteDataSource
) : RatingRepository {
    override fun getRating(courseId: Long, count: Int, days: Int): Single<List<RatingItem>> =
        ratingRemoteDataSource.getRating(courseId, count, days)

    override fun putRating(courseId: Long, exp: Long): Completable =
        ratingRemoteDataSource.putRating(courseId, exp)

    override fun restoreRating(courseId: Long): Single<Long> =
        ratingRemoteDataSource.restoreRating(courseId)
}