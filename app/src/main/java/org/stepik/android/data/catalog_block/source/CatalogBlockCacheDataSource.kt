package org.stepik.android.data.catalog_block.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.catalog_block.model.CatalogBlock

interface CatalogBlockCacheDataSource {
    fun getCatalogBlocks(language: String): Single<List<CatalogBlock>>
    fun insertCatalogBlocks(catalogBlocks: List<CatalogBlock>): Completable
}