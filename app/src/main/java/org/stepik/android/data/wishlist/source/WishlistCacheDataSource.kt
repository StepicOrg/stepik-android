package org.stepik.android.data.wishlist.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntry

interface WishlistCacheDataSource {
    fun getWishlistEntry(courseId: Long): Maybe<WishlistEntry>
    fun getWishlistEntries(): Single<List<WishlistEntry>>
    fun saveWishlistEntry(wishlistEntry: WishlistEntry): Completable
    fun saveWishlistEntries(wishlistEntries: List<WishlistEntry>): Completable
    fun removeWishlistEntry(courseId: Long): Completable
    fun removeWishlistEntries(): Completable
}