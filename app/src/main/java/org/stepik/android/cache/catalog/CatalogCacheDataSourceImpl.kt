package org.stepik.android.cache.catalog

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.catalog.dao.CatalogBlockDao
import org.stepik.android.data.catalog.source.CatalogCacheDataSource
import org.stepik.android.domain.catalog.model.CatalogBlock
import javax.inject.Inject

class CatalogCacheDataSourceImpl
@Inject
constructor(
    private val catalogBlockDao: CatalogBlockDao
) : CatalogCacheDataSource {
    override fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>> =
        catalogBlockDao
            .getCatalogBlocks(language)
            .filter(List<CatalogBlock>::isNotEmpty)

    override fun insertCatalogBlocks(catalogBlocks: List<CatalogBlock>): Completable =
        catalogBlockDao
            .clearCatalogBlocks()
            .andThen(catalogBlockDao.insertCatalogBlocks(catalogBlocks))
}