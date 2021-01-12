package org.stepik.android.remote.catalog

import io.reactivex.Maybe
import org.stepik.android.data.catalog.source.CatalogRemoteDataSource
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.remote.catalog.model.CatalogBlockResponse
import org.stepik.android.remote.catalog.service.CatalogService
import javax.inject.Inject

class CatalogRemoteDataSourceImpl
@Inject
constructor(
    private val catalogService: CatalogService
) : CatalogRemoteDataSource {
    override fun getCatalogBlocks(language: String): Maybe<List<CatalogBlock>> =
        catalogService
            .getCatalogBlocks(mapOf("language" to language)) // TODO Switch to a "query" object
            .map(CatalogBlockResponse::catalogBlocks)
}