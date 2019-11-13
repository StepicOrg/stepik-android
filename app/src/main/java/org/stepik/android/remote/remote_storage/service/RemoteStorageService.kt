package org.stepik.android.remote.remote_storage.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRequest
import org.stepic.droid.web.storage.model.StorageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteStorageService {

    @GET("api/storage-records")
    fun getStorageRecords(
        @Query("page") page: Int,
        @Query("user") userId: Long,
        @Query("kind") kind: String? = null,
        @Query("kind__startswith") startsWith: String? = null
    ): Single<StorageResponse>

    @POST("api/storage-records")
    fun createStorageRecord(
        @Body body: StorageRequest
    ): Single<StorageResponse>

    @PUT("api/storage-records/{recordId}")
    fun setStorageRecord(
        @Path("recordId") recordId: Long,
        @Body body: StorageRequest
    ): Single<StorageResponse>

    @DELETE("api/storage-records/{recordId}")
    fun removeStorageRecord(
        @Path("recordId") recordId: Long
    ): Completable
}
