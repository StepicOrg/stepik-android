package org.stepik.android.cache.mobile_tiers.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.mobile_tiers.model.MobileTier

@Dao
interface MobileTiersDao {
    @Query("SELECT * FROM MobileTier WHERE course IN (:ids)")
    fun getMobileTiers(ids: List<Long>): Single<List<MobileTier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMobileTiers(items: List<MobileTier>): Completable
}