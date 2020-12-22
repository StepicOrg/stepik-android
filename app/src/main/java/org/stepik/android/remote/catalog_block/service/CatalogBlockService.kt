package org.stepik.android.remote.catalog_block.service

import io.reactivex.Maybe
import org.stepik.android.remote.catalog_block.model.CatalogBlockResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CatalogBlockService {
    @GET("api/catalog-blocks")
    fun getCatalogBlocks(@QueryMap query: Map<String, String>): Maybe<CatalogBlockResponse>
}