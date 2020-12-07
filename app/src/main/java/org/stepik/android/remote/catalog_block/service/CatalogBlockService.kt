package org.stepik.android.remote.catalog_block.service

import io.reactivex.Single
import org.stepik.android.remote.catalog_block.model.CatalogBlockResponse
import retrofit2.http.GET

interface CatalogBlockService {
    // TODO Add platform and language query parameters
    @GET("api/catalog-blocks?language=ru")
    fun getCatalogBlocks(): Single<CatalogBlockResponse>
}