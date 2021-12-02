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
            .createWishlistEntry(courseId)
            .doCompletableOnSuccess(wishlistCacheDataSource::saveWishlistEntry)
            .ignoreElement()

    override fun removeCourseFromWishlist(courseId: Long, sourceType: DataSourceType): Completable =
        when (sourceType) {
            DataSourceType.REMOTE ->
                wishlistCacheDataSource
                    .getWishlistEntry(courseId)
                    .switchIfEmpty(wishlistRemoteDataSource.getWishlistEntry(courseId))
                    .flatMapCompletable { wishlistEntry -> wishlistRemoteDataSource.removeWishlistEntry(wishlistEntry.id) }
                    .andThen(wishlistCacheDataSource.removeWishlistEntry(courseId))

            DataSourceType.CACHE ->
                wishlistCacheDataSource.removeWishlistEntry(courseId)

            else ->
                throw IllegalArgumentException("Unsupported sourceType = $sourceType")
        }

    override fun removeWishlistEntries(): Completable =
        wishlistCacheDataSource.removeWishlistEntries()
}