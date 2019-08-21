package org.stepik.android.data.certificates.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.certificates.source.CertificatesCacheDataSource
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesRepositoryImpl
@Inject
constructor(
    private val certificatesCacheDataSource: CertificatesCacheDataSource,
    private val certificatesRemoteDataSource: CertificatesRemoteDataSource
) : CertificatesRepository {
    override fun getCertificates(userId: Long, page: Int, sourceType: DataSourceType): Single<PagedList<Certificate>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                certificatesRemoteDataSource
                    .getCertificates(userId)
                    .doCompletableOnSuccess(certificatesCacheDataSource::saveCertificates)

            DataSourceType.CACHE ->
                certificatesCacheDataSource
                    .getCertificates(userId)

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
}