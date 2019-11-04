package org.stepik.android.view.injection.email_address

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.network.NetworkHelper
import org.stepik.android.cache.email_address.EmailAddressCacheDataSourceImpl
import org.stepik.android.data.email_address.repository.EmailAddressRepositoryImpl
import org.stepik.android.data.email_address.source.EmailAddressCacheDataSource
import org.stepik.android.data.email_address.source.EmailAddressRemoteDataSource
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.remote.email_address.EmailAddressRemoteDataSourceImpl
import org.stepik.android.remote.email_address.service.EmailAddressService

@Module
abstract class EmailAddressDataModule {
    @Binds
    internal abstract fun bindEmailAddressRepository(
        emailAddressRepositoryImpl: EmailAddressRepositoryImpl
    ): EmailAddressRepository

    @Binds
    internal abstract fun bindEmailAddressRemoteDataSource(
        emailAddressRemoteDataSourceImpl: EmailAddressRemoteDataSourceImpl
    ): EmailAddressRemoteDataSource

    @Binds
    internal abstract fun bindEmailAddressCacheDataSource(
        emailAddressCacheDataSourceImpl: EmailAddressCacheDataSourceImpl
    ): EmailAddressCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideEmailAddressService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): EmailAddressService =
            NetworkHelper.createService(interceptors, config.baseUrl)
    }
}