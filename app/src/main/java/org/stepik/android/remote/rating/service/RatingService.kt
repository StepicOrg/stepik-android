package org.stepik.android.remote.rating.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.rating.model.RatingRequest
import org.stepik.android.remote.rating.model.RatingResponse
import org.stepik.android.remote.rating.model.RatingRestoreResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface RatingService {
    @PUT("rating")
    fun putRating(
        @Body ratingRequest: RatingRequest
    ): Completable

    @GET("rating")
    fun getRating(
        @Query("course") courseId: Long,
        @Query("count") count: Int,
        @Query("days") days: Int,
        @Query("user") userId: Long
    ): Single<RatingResponse>

    @GET("rating-restore")
    fun restoreRating(
        @Query("course") courseId: Long,
        @Query("token") token: String?
    ): Single<RatingRestoreResponse>
}