package org.stepik.android.remote.catalog_block.service

import io.reactivex.Single
import org.stepik.android.remote.catalog_block.model.CatalogBlockResponse
import retrofit2.http.GET

interface CatalogBlockService {
    @GET("api/catalog-blocks")
    fun getCatalogBlocks(): Single<CatalogBlockResponse>
}