package org.stepik.android.view.injection.certificate

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.certificates.source.CertificateCacheDataSourceImpl
import org.stepik.android.data.certificate.repository.CertificateRepositoryImpl
import org.stepik.android.data.certificate.source.CertificateCacheDataSource
import org.stepik.android.data.certificate.source.CertificateRemoteDataSource
import org.stepik.android.domain.certificate.repository.CertificateRepository
import org.stepik.android.remote.certificate.service.CertificateService
import org.stepik.android.remote.certificate.source.CertificateRemoteDataSourceImpl
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCertificateService(@Authorized retrofit: Retrofit): CertificateService =
            retrofit.create(CertificateService::class.java)
    }
}