package org.stepik.android.domain.catalog.repository

import io.reactivex.Maybe
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogBlock

interface CatalogRepository {
    fun getCatalogBlocks(language: String, primarySourceType: DataSourceType = DataSourceType.REMOTE): Maybe<List<CatalogBlock>>
}