package org.stepik.android.data.rating.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.adaptive.RatingItem
import org.stepik.android.remote.rating.model.RatingRestoreResponse

interface RatingRemoteDataSource {
    fun getRating(courseId: Long, count: Int, days: Int): Single<List<RatingItem>>
    fun putRating(courseId: Long, exp: Long): Completable
    fun restoreRating(courseId: Long): Single<RatingRestoreResponse>
}