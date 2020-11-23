package org.stepik.android.domain.catalog_block.repository

import io.reactivex.Single
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem

interface CatalogBlockRepository {
    fun getCatalogBlocks(): Single<List<CatalogBlockItem>>
}