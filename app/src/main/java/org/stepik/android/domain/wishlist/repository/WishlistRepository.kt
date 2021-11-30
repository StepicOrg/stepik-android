package org.stepik.android.domain.wishlist.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistEntry

interface WishlistRepository {
    fun getWishlistEntries(sourceType: DataSourceType): Single<List<WishlistEntry>>
    fun addCourseToWishlist(courseId: Long): Completable
    fun removeCourseFromWishlist(courseId: Long, sourceType: DataSourceType): Completable
    fun removeWishlistEntries(): Completable
}