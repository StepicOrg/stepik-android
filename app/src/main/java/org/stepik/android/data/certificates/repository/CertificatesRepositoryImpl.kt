package org.stepik.android.data.certificates.repository

import io.reactivex.Single
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
    override fun getCertificates(userId: Long, primarySourceType: DataSourceType): Single<List<Certificate>> {
        val remoteSource = certificatesRemoteDataSource
            .getCertificates(userId)
            .doCompletableOnSuccess(certificatesCacheDataSource::saveCertificates)

        val cacheSource = certificatesCacheDataSource
            .getCertificates(userId)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource

            DataSourceType.CACHE ->
                cacheSource

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}