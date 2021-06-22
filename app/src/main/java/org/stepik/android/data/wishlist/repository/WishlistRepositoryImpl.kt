package org.stepik.android.data.wishlist.repository

import io.reactivex.Single
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.data.wishlist.source.WishlistRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class WishlistRepositoryImpl
@Inject
constructor(
    private val wishlistRemoteDataSource: WishlistRemoteDataSource,
    private val wishlistCacheDataSource: WishlistCacheDataSource
) : WishlistRepository {
    override fun updateWishlistRecord(wishlistEntity: WishlistEntity): Single<WishlistEntity> =
        wishlistRemoteDataSource
            .updateWishlistRecord(wishlistEntity)
            .doCompletableOnSuccess(wishlistCacheDataSource::saveWishlistRecord)

    override fun getWishlistRecord(sourceType: DataSourceType): Single<WishlistEntity> {
        val remote = wishlistRemoteDataSource
            .getWishlistRecord()
            .doCompletableOnSuccess(wishlistCacheDataSource::saveWishlistRecord)

        val cache = wishlistCacheDataSource.getWishlistRecord()

        return when (sourceType) {
            DataSourceType.REMOTE ->
                remote.onErrorResumeNext(cache.toSingle())

            DataSourceType.CACHE ->
                    cache.switchIfEmpty(remote)

            else ->
                throw IllegalArgumentException("Unsupported sourceType = $sourceType")
        }
    }
}