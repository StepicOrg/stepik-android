package org.stepik.android.cache.catalog_block.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem

@Dao
interface CatalogBlockDao {
    @Query("SELECT * FROM CatalogBlockItem")
    fun getCatalogBlocks(): Single<List<CatalogBlockItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogBlocks(catalogBlocks: List<CatalogBlockItem>): Completable

    @Query("DELETE FROM CatalogBlockItem")
    fun clearCatalogBlocks(): Completable
}