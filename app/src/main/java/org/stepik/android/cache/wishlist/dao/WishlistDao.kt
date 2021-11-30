package org.stepik.android.cache.wishlist.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntry

@Dao
abstract class WishlistDao {
    @Query("SELECT * FROM `WishlistEntry` WHERE course == :courseId LIMIT 1")
    abstract fun getWishlistEntry(courseId: Long): Maybe<WishlistEntry>

    @Query("SELECT * FROM WishlistEntry ORDER BY createDate DESC")
    abstract fun getWishlistEntries(): Single<List<WishlistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWishlistEntry(wishlistEntry: WishlistEntry): Completable

    @Transaction
    open fun insertWishlistEntriesNew(wishlistEntries: List<WishlistEntry>) {
        clearTable()
        insertWishlistEntries(wishlistEntries)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWishlistEntries(wishlistEntries: List<WishlistEntry>)

    @Query("DELETE FROM `WishlistEntry` WHERE course == :courseId")
    abstract fun deleteWishlistEntry(courseId: Long): Completable

    @Query("DELETE FROM `WishlistEntry`")
    abstract fun clearTable()
}