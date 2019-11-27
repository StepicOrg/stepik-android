package org.stepik.android.remote.certificate.service

import io.reactivex.Single
import org.stepik.android.remote.certificate.model.CertificateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CertificateService {
    @GET("api/certificates")
    fun getCertificates(@Query("user") userId: Long, @Query("page") page: Int): Single<CertificateResponse>
}