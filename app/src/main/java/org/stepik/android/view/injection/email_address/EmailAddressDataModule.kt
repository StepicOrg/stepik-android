package org.stepik.android.view.injection.email_address

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.email_address.repository.EmailAddressRepositoryImpl
import org.stepik.android.data.email_address.source.EmailAddressRemoteDataSource
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.remote.email_address.EmailAddressRemoteDataSourceImpl
import org.stepik.android.remote.email_address.service.EmailAddressService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideEmailAddressService(@Authorized retrofit: Retrofit): EmailAddressService =
            retrofit.create(EmailAddressService::class.java)
    }
}