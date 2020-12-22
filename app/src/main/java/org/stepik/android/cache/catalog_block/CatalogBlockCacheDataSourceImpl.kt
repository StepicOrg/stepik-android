package org.stepik.android.cache.catalog_block

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.catalog_block.dao.CatalogBlockDao
import org.stepik.android.data.catalog_block.source.CatalogBlockCacheDataSource
import org.stepik.android.domain.catalog_block.model.CatalogBlock
import javax.inject.Inject

class CatalogBlockCacheDataSourceImpl
@Inject
constructor(
    private val catalogBlockDao: CatalogBlockDao
) : CatalogBlockCacheDataSource {
    override fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>> =
        catalogBlockDao
            .getCatalogBlocks(language)
            .filter(List<CatalogBlock>::isNotEmpty)

    override fun insertCatalogBlocks(catalogBlocks: List<CatalogBlock>): Completable =
        catalogBlockDao
            .clearCatalogBlocks()
            .andThen(catalogBlockDao.insertCatalogBlocks(catalogBlocks))
}