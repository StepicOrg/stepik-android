package org.stepik.android.data.certificate.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.certificate.source.CertificateCacheDataSource
import org.stepik.android.data.certificate.source.CertificateRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificate.repository.CertificateRepository
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificateRepositoryImpl
@Inject
constructor(
    private val certificateCacheDataSource: CertificateCacheDataSource,
    private val certificateRemoteDataSource: CertificateRemoteDataSource
) : CertificateRepository {
    override fun getCertificates(userId: Long, page: Int, sourceType: DataSourceType): Single<PagedList<Certificate>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                certificateRemoteDataSource
                    .getCertificates(userId)
                    .doCompletableOnSuccess(certificateCacheDataSource::saveCertificates)

            DataSourceType.CACHE ->
                certificateCacheDataSource
                    .getCertificates(userId)

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
}