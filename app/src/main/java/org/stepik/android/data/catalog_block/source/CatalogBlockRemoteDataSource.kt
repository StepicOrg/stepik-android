package org.stepik.android.data.catalog_block.source

import io.reactivex.Single
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem

interface CatalogBlockRemoteDataSource {
    fun getCatalogBlocks(): Single<List<CatalogBlockItem>>
}