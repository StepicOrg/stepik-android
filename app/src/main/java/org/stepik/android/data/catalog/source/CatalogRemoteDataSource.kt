package org.stepik.android.data.catalog.source

import io.reactivex.Maybe
import org.stepik.android.domain.catalog.model.CatalogBlock

interface CatalogRemoteDataSource {
    fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>>
}