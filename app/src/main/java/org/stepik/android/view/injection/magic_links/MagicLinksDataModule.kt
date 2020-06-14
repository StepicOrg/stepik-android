package org.stepik.android.view.injection.magic_links

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.magic_links.repository.MagicLinksRepositoryImpl
import org.stepik.android.data.magic_links.source.MagicLinksRemoteDataSource
import org.stepik.android.domain.magic_links.repository.MagicLinksRepository
import org.stepik.android.remote.magic_links.MagicLinksRemoteDataSourceImpl
import org.stepik.android.remote.magic_links.service.MagicLinksService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class MagicLinksDataModule {
    @Binds
    internal abstract fun bindMagicLinksRepository(
        magicLinksRepositoryImpl: MagicLinksRepositoryImpl
    ): MagicLinksRepository

    @Binds
    internal abstract fun bindMagicLinksRemoteDataSource(
        magicLinksRemoteDataSource: MagicLinksRemoteDataSourceImpl
    ): MagicLinksRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCoursePaymentService(@Authorized retrofit: Retrofit): MagicLinksService =
            retrofit.create(MagicLinksService::class.java)
    }
}