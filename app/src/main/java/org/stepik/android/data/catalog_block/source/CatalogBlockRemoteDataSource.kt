package org.stepik.android.data.catalog_block.source

import io.reactivex.Maybe
import org.stepik.android.domain.catalog_block.model.CatalogBlock

interface CatalogBlockRemoteDataSource {
    fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>>
}