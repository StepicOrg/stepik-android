package org.stepik.android.cache.wishlist

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.domain.wishlist.model.WishlistEntity
import javax.inject.Inject

class WishlistCacheDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : WishlistCacheDataSource {
    override fun getWishlistRecord(): Maybe<WishlistEntity> =
        Maybe.fromCallable { sharedPreferenceHelper.wishlist }

    override fun saveWishlistRecord(wishlistEntity: WishlistEntity): Completable =
        Completable
            .fromAction {
                sharedPreferenceHelper
                    .storeWishlist(wishlistEntity)
            }
}