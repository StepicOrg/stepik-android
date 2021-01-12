package org.stepik.android.data.catalog.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.catalog.model.CatalogBlock

interface CatalogCacheDataSource {
    fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>>
    fun insertCatalogBlocks(catalogBlocks: List<CatalogBlock>): Completable
}