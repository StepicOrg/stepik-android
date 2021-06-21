package org.stepik.android.cache.wishlist

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.wishlist.mapper.WishlistEntityMapper
import org.stepik.android.data.wishlist.KIND_WISHLIST
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.remote.wishlist.model.WishlistWrapper
import javax.inject.Inject

class WishlistCacheDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val wishlistEntityMapper: WishlistEntityMapper
) : WishlistCacheDataSource {
    override fun getWishlistRecord(): Maybe<StorageRecord<WishlistWrapper>> =
        Maybe
            .fromCallable { sharedPreferenceHelper.wishlist }
            .map {
                StorageRecord(
                    id = it.recordId,
                    kind = KIND_WISHLIST,
                    data = WishlistWrapper(it.courses)
                )
            }

    override fun saveWishlistRecord(wishlistRecord: StorageRecord<WishlistWrapper>): Completable =
        Completable
            .fromAction {
                sharedPreferenceHelper
                    .storeWishlist(wishlistEntityMapper.mapToEntity(wishlistRecord))
            }
}