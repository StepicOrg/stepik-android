package org.stepik.android.cache.wishlist

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.cache.wishlist.dao.WishlistDao
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.domain.wishlist.model.WishlistEntry
import javax.inject.Inject

class WishlistCacheDataSourceImpl
@Inject
constructor(
    private val wishlistDao: WishlistDao,
    private val databaseFacade: DatabaseFacade
) : WishlistCacheDataSource {
    override fun getWishlistEntry(courseId: Long): Maybe<WishlistEntry> =
        wishlistDao.getWishlistEntry(courseId)

    override fun getWishlistEntries(): Single<List<WishlistEntry>> =
        wishlistDao.getWishlistEntries()

    override fun saveWishlistEntry(wishlistEntry: WishlistEntry): Completable =
        wishlistDao
            .insertWishlistEntry(wishlistEntry)
            .andThen(updateCourseIsInWishlist(wishlistEntry.course, isInWishList = true))

    override fun saveWishlistEntries(wishlistEntries: List<WishlistEntry>): Completable =
        Completable.fromCallable {
            wishlistDao.insertWishlistEntriesNew(wishlistEntries)
        }

    override fun removeWishlistEntry(courseId: Long): Completable =
        wishlistDao
            .deleteWishlistEntry(courseId)
            .andThen(updateCourseIsInWishlist(courseId, isInWishList = false))

    override fun removeWishlistEntries(): Completable =
        Completable.fromCallable {
            wishlistDao.clearTable()
        }

    private fun updateCourseIsInWishlist(courseId: Long, isInWishList: Boolean): Completable =
        Completable.fromAction {
            databaseFacade.updateCourseIsInWishlist(courseId, isInWishList)
        }
}