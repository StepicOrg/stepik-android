package org.stepik.android.view.injection.profile

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.profile.ProfileCacheDataSourceImpl
import org.stepik.android.data.profile.repository.ProfileRepositoryImpl
import org.stepik.android.data.profile.source.ProfileCacheDataSource
import org.stepik.android.data.profile.source.ProfileRemoteDataSource
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.remote.profile.ProfileRemoteDataSourceImpl
import org.stepik.android.remote.profile.service.ProfileService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class ProfileDataModule {
    @Binds
    internal abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    internal abstract fun bindProfileRemoteDataSource(
        profileRemoteDataSourceImpl: ProfileRemoteDataSourceImpl
    ): ProfileRemoteDataSource

    @Binds
    internal abstract fun bindProfileCacheDataSource(
        profileCacheDataSourceImpl: ProfileCacheDataSourceImpl
    ): ProfileCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideProfileService(@Authorized retrofit: Retrofit): ProfileService =
            retrofit.create(ProfileService::class.java)
    }
}