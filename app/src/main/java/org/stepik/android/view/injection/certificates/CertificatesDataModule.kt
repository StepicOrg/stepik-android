package org.stepik.android.view.injection.certificates

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.certificates.source.CertificatesCacheDataSourceImpl
import org.stepik.android.data.certificates.repository.CertificatesRepositoryImpl
import org.stepik.android.data.certificates.source.CertificatesCacheDataSource
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.remote.certificates.source.CertificatesRemoteDataSourceImpl

@Module
abstract class CertificatesDataModule {
    @Binds
    internal abstract fun bindCertificatesRepository(
        certificatesRepositoryImpl: CertificatesRepositoryImpl
    ): CertificatesRepository

    @Binds
    internal abstract fun bindCertificatesRemoteDataSource(
        certificatesRemoteDataSourceImplImpl: CertificatesRemoteDataSourceImpl
    ): CertificatesRemoteDataSource

    @Binds
    internal abstract fun bindCertificatesCacheDataSource(
        certificatesCacheDataSourceImpl: CertificatesCacheDataSourceImpl
    ): CertificatesCacheDataSource
}