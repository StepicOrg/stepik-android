package org.stepik.android.data.wishlist.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntry

interface WishlistRemoteDataSource {
    fun getWishlistEntries(): Single<List<WishlistEntry>>
    fun saveWishlistEntry(courseId: Long): Single<WishlistEntry>
    fun removeWishlistEntry(wishlistEntryId: Long): Completable
}