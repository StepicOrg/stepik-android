package org.stepik.android.cache.wishlist.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntry

@Dao
interface WishlistDao {
    @Query("SELECT * FROM `WishlistEntry` WHERE course == :courseId LIMIT 1")
    fun getWishlistEntry(courseId: Long): Single<WishlistEntry>

    @Query("SELECT * FROM WishlistEntry ORDER BY createDate DESC")
    fun getWishlistEntries(): Single<List<WishlistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWishlistEntry(wishlistEntry: WishlistEntry): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWishlistEntries(wishlistEntries: List<WishlistEntry>): Completable

    @Query("DELETE FROM `WishlistEntry` WHERE course == :courseId")
    fun deleteWishlistEntry(courseId: Long): Completable
}