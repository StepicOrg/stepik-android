package org.stepik.android.data.certificate.repository

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.domain.rx.doCompletableOnSuccess
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
    override fun getCertificate(userId: Long, courseId: Long, sourceType: DataSourceType): Maybe<Certificate> {
        val remoteSource = certificateRemoteDataSource
            .getCertificate(userId, courseId)
            .doCompletableOnSuccess(certificateCacheDataSource::saveCertificate)

        val cacheSource = certificateCacheDataSource
            .getCertificate(userId, courseId)

        return when (sourceType) {
            DataSourceType.CACHE ->
                cacheSource.switchIfEmpty(remoteSource)

            DataSourceType.REMOTE ->
                remoteSource

            else -> throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
    }

    override fun getCertificates(userId: Long, page: Int, sourceType: DataSourceType): Single<PagedList<Certificate>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                certificateRemoteDataSource
                    .getCertificates(userId, page)
                    .doCompletableOnSuccess(certificateCacheDataSource::saveCertificates)

            DataSourceType.CACHE ->
                certificateCacheDataSource
                    .getCertificates(userId)

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }

    override fun updateCertificate(certificate: Certificate): Single<Certificate> =
        certificateRemoteDataSource
            .updateCertificate(certificate)
            .doCompletableOnSuccess(certificateCacheDataSource::saveCertificate)
}