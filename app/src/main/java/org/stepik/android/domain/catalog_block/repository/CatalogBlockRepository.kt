package org.stepik.android.domain.catalog_block.repository

import io.reactivex.Maybe
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog_block.model.CatalogBlock

interface CatalogBlockRepository {
    fun getCatalogBlocks(language: String, primarySourceType: DataSourceType = DataSourceType.REMOTE): Maybe<List<CatalogBlock>>
}