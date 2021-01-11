package org.stepik.android.cache.catalog.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.catalog.model.CatalogBlock

@Dao
interface CatalogBlockDao {
    @Query("SELECT * FROM CatalogBlock WHERE language = :language")
    fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogBlocks(catalogBlocks: List<CatalogBlock>): Completable

    @Query("DELETE FROM CatalogBlock")
    fun clearCatalogBlocks(): Completable
}