package org.stepik.android.data.wishlist.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.data.wishlist.source.WishlistRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistEntry
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class WishlistRepositoryImpl
@Inject
constructor(
    private val wishlistRemoteDataSource: WishlistRemoteDataSource,
    private val wishlistCacheDataSource: WishlistCacheDataSource
) : WishlistRepository {
    override fun getWishlistEntries(sourceType: DataSourceType): Single<List<WishlistEntry>> {
        val remote = wishlistRemoteDataSource
            .getWishlistEntries()
            .doCompletableOnSuccess(wishlistCacheDataSource::saveWishlistEntries)

        val cache = wishlistCacheDataSource.getWishlistEntries()

        return when (sourceType) {
            DataSourceType.REMOTE ->
                remote.onErrorResumeNext(cache)

            DataSourceType.CACHE ->
                cache

            else ->
                throw IllegalArgumentException("Unsupported sourceType = $sourceType")
        }
    }

    override fun addCourseToWishlist(courseId: Long): Completable =
        wishlistRemoteDataSource
            .saveWishlistEntry(courseId)
            .doCompletableOnSuccess(wishlistCacheDataSource::saveWishlistEntry)
            .ignoreElement()

    /**
     * If removal is manual - notify server about removal, else just remove locally
     */
    override fun removeCourseFromWishlist(courseId: Long, isManualRemoval: Boolean): Completable =
        if (isManualRemoval) {
            wishlistCacheDataSource
                .getWishlistEntry(courseId)
                .flatMapCompletable { wishlistEntry -> wishlistRemoteDataSource.removeWishlistEntry(wishlistEntry.id) }
                .andThen(wishlistCacheDataSource.removeWishlistEntry(courseId))
        } else {
            wishlistCacheDataSource.removeWishlistEntry(courseId)
        }
}