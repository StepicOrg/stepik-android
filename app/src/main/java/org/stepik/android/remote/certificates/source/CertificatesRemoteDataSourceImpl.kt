package org.stepik.android.remote.certificates.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepic.droid.web.CertificateResponse
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CertificatesRemoteDataSource {
    // TODO Replace with CertificateRespinse from stepik package
    private val certificatesResponseMapper =
        Function<CertificateResponse, List<Certificate>>(CertificateResponse::certificates)

    override fun getCertificates(userId: Long): Single<List<Certificate>> =
        api.getCertificatesReactive(userId)
            .map(certificatesResponseMapper)
}