package org.stepik.android.cache.wishlist

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.wishlist.dao.WishlistDao
import org.stepik.android.cache.wishlist.mapper.WishlistEntityMapper
import org.stepik.android.data.wishlist.getKindWishlist
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import javax.inject.Inject

class WishlistCacheDataSourceImpl
@Inject
constructor(
    private val wishlistDao: WishlistDao,
    private val wishlistEntityMapper: WishlistEntityMapper
) : WishlistCacheDataSource {
    override fun getWishlistRecord(): Maybe<StorageRecord<WishlistWrapper>> =
        wishlistDao
            .getWishlistEntity()
            .map {
                StorageRecord(
                    id = it.recordId,
                    kind = getKindWishlist(),
                    data = WishlistWrapper(it.courses)
                )
            }

    override fun saveWishlistRecord(wishlistRecord: StorageRecord<WishlistWrapper>): Completable =
        wishlistDao
            .saveWishlistEntity(wishlistEntityMapper.mapToEntity(wishlistRecord))
}