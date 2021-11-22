package org.stepik.android.remote.certificate.service

import io.reactivex.Single
import org.stepik.android.remote.certificate.model.CertificateRequest
import org.stepik.android.remote.certificate.model.CertificateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CertificateService {
    @GET("api/certificates")
    fun getCertificates(@Query("user") userId: Long, @Query("page") page: Int): Single<CertificateResponse>

    @GET("api/certificates")
    fun getCertificate(@Query("user") userId: Long, @Query("course") courseId: Long): Single<CertificateResponse>

    @PUT("api/certificates/{certificateId}")
    fun updateCertificate(@Path("certificateId") certificateId: Long, @Body certificateRequest: CertificateRequest): Single<CertificateResponse>
}