package org.stepik.android.remote.rating

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.RatingService
import org.stepic.droid.web.model.adaptive.RatingRequest
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse
import org.stepik.android.data.rating.source.RatingRemoteDataSource
import org.stepik.android.model.adaptive.RatingItem
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

    override fun restoreRating(courseId: Long): Single<RatingRestoreResponse> =
        ratingService.restoreRating(courseId, getAccessToken())

    private fun getCurrentUserId(): Long =
        sharedPreferenceHelper.profile?.id ?: 0

    private fun getAccessToken(): String? =
        sharedPreferenceHelper.authResponseFromStore?.accessToken
}