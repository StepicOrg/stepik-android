package org.stepik.android.remote.rating

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.data.rating.source.RatingRemoteDataSource
import org.stepik.android.model.adaptive.RatingItem
import org.stepik.android.remote.rating.model.RatingRequest
import org.stepik.android.remote.rating.model.RatingRestoreResponse
import org.stepik.android.remote.rating.service.RatingService
import javax.inject.Inject

class RatingRemoteDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val ratingService: RatingService
) : RatingRemoteDataSource {
    override fun getRating(courseId: Long, count: Int, days: Int): Single<List<RatingItem>> =
        ratingService.getRating(courseId, count, days, getCurrentUserId()).map { it.users }

    override fun putRating(courseId: Long, exp: Long): Completable =
        ratingService.putRating(RatingRequest(exp, courseId, getAccessToken()))

    override fun restoreRating(courseId: Long): Single<Long> =
        ratingService.restoreRating(courseId, getAccessToken()).map(RatingRestoreResponse::exp)

    private fun getCurrentUserId(): Long =
        sharedPreferenceHelper.profile?.id ?: 0

    private fun getAccessToken(): String? =
        sharedPreferenceHelper.authResponseFromStore?.accessToken
}