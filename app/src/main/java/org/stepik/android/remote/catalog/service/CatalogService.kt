package org.stepik.android.remote.catalog.service

import io.reactivex.Maybe
import org.stepik.android.remote.catalog.model.CatalogBlockResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CatalogService {
    @GET("api/catalog-blocks?platform=mobile,android")
    fun getCatalogBlocks(@QueryMap query: Map<String, String>): Maybe<CatalogBlockResponse>
}