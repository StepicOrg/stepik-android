package org.stepik.android.data.wishlist.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntry

interface WishlistRemoteDataSource {
    fun getWishlistEntry(courseId: Long): Maybe<WishlistEntry>
    fun getWishlistEntries(): Single<List<WishlistEntry>>
    fun createWishlistEntry(courseId: Long): Single<WishlistEntry>
    fun removeWishlistEntry(wishlistEntryId: Long): Completable
}