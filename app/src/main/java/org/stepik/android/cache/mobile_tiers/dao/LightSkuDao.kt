package org.stepik.android.cache.mobile_tiers.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.mobile_tiers.model.LightSku

@Dao
interface LightSkuDao {
    @Query("SELECT * From LightSku WHERE id IN (:ids)")
    fun getLightSkus(ids: List<String>): Single<List<LightSku>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveLightSkus(items: List<LightSku>): Completable
}