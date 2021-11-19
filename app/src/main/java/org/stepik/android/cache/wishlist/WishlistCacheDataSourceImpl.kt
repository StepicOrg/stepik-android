package org.stepik.android.cache.wishlist

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.wishlist.dao.WishlistDao
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.domain.wishlist.model.WishlistEntry
import javax.inject.Inject

class WishlistCacheDataSourceImpl
@Inject
constructor(
    private val wishlistDao: WishlistDao
) : WishlistCacheDataSource {
    override fun getWishlistEntry(courseId: Long): Single<WishlistEntry> =
        wishlistDao.getWishlistEntry(courseId)

    override fun getWishlistEntries(): Single<List<WishlistEntry>> =
        wishlistDao.getWishlistEntries()

    override fun saveWishlistEntry(wishlistEntry: WishlistEntry): Completable =
        wishlistDao.insertWishlistEntry(wishlistEntry)

    override fun saveWishlistEntries(wishlistEntries: List<WishlistEntry>): Completable =
        wishlistDao.insertWishlistEntries(wishlistEntries)

    override fun removeWishlistEntry(courseId: Long): Completable =
        wishlistDao.deleteWishlistEntry(courseId)

    override fun removeWishlistEntries(): Completable =
        wishlistDao.clearTable()
}