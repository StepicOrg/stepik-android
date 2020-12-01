package org.stepik.android.data.catalog_block.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem

interface CatalogBlockCacheDataSource {
    fun getCatalogBlocks(): Single<List<CatalogBlockItem>>
    fun insertCatalogBlocks(catalogBlocks: List<CatalogBlockItem>): Completable
}