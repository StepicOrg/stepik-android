package org.stepik.android.remote.certificate.source

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import org.stepik.android.data.certificate.source.CertificateRemoteDataSource
import org.stepik.android.model.Certificate
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.certificate.model.CertificateRequest
import org.stepik.android.remote.certificate.model.CertificateResponse
import org.stepik.android.remote.certificate.service.CertificateService
import ru.nobird.android.domain.rx.first
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class CertificateRemoteDataSourceImpl
@Inject
constructor(
    private val certificateService: CertificateService
) : CertificateRemoteDataSource {
    override fun getCertificate(userId: Long, courseId: Long): Maybe<Certificate> =
        certificateService
            .getCertificate(userId, courseId)
            .map(CertificateResponse::certificates)
            .maybeFirst()

    override fun getCertificates(userId: Long, page: Int): Single<PagedList<Certificate>> =
        certificateService.getCertificates(userId, page)
            .map { it.toPagedList(CertificateResponse::certificates) }

    override fun updateCertificate(certificate: Certificate): Single<Certificate> =
        certificateService
            .updateCertificate(certificate.id, CertificateRequest(certificate))
            .map(CertificateResponse::certificates)
            .first()
}