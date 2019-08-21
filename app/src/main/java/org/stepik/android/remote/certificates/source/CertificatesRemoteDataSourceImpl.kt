package org.stepik.android.remote.certificates.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.web.Api
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.model.Certificate
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.certificates.model.CertificateResponse
import javax.inject.Inject

class CertificatesRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CertificatesRemoteDataSource {
    override fun getCertificates(userId: Long, page: Int): Single<PagedList<Certificate>> =
        api.getCertificatesReactive(userId, page)
            .map { it.toPagedList(CertificateResponse::certificates)}
}