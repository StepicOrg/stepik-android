package org.stepik.android.remote.certificates.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.model.Certificate
import org.stepik.android.remote.certificates.model.CertificateResponse
import javax.inject.Inject

class CertificatesRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CertificatesRemoteDataSource {
    private val certificatesResponseMapper =
        Function<CertificateResponse, List<Certificate>>(CertificateResponse::certificates)

    override fun getCertificates(userId: Long): Single<List<Certificate>> =
        api.getCertificatesReactive(userId)
            .map(certificatesResponseMapper)
}