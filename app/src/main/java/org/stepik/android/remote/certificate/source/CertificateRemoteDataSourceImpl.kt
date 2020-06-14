package org.stepik.android.remote.certificate.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.certificate.source.CertificateRemoteDataSource
import org.stepik.android.model.Certificate
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.certificate.model.CertificateResponse
import org.stepik.android.remote.certificate.service.CertificateService
import javax.inject.Inject

class CertificateRemoteDataSourceImpl
@Inject
constructor(
    private val certificateService: CertificateService
) : CertificateRemoteDataSource {
    override fun getCertificates(userId: Long, page: Int): Single<PagedList<Certificate>> =
        certificateService.getCertificates(userId, page)
            .map { it.toPagedList(CertificateResponse::certificates) }
}