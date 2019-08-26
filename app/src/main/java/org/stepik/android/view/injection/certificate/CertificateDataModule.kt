package org.stepik.android.view.injection.certificate

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.certificates.source.CertificateCacheDataSourceImpl
import org.stepik.android.data.certificate.repository.CertificateRepositoryImpl
import org.stepik.android.data.certificate.source.CertificateCacheDataSource
import org.stepik.android.data.certificate.source.CertificateRemoteDataSource
import org.stepik.android.domain.certificate.repository.CertificateRepository
import org.stepik.android.remote.certificate.source.CertificateRemoteDataSourceImpl

@Module
abstract class CertificateDataModule {
    @Binds
    internal abstract fun bindCertificatesRepository(
        certificatesRepositoryImpl: CertificateRepositoryImpl
    ): CertificateRepository

    @Binds
    internal abstract fun bindCertificatesRemoteDataSource(
        certificatesRemoteDataSourceImplImpl: CertificateRemoteDataSourceImpl
    ): CertificateRemoteDataSource

    @Binds
    internal abstract fun bindCertificatesCacheDataSource(
        certificatesCacheDataSourceImpl: CertificateCacheDataSourceImpl
    ): CertificateCacheDataSource
}