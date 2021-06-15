package org.stepik.android.cache.wishlist.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.wishlist.model.WishlistEntity

@Dao
interface WishlistDao {
    @Query("SELECT * FROM WishlistEntity LIMIT 1")
    fun getWishlistEntity(): Maybe<WishlistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWishlistEntity(wishlistEntity: WishlistEntity): Completable
}