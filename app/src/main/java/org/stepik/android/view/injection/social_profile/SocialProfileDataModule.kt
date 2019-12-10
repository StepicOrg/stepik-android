package org.stepik.android.view.injection.social_profile

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.social_profile.SocialProfileCacheDataSourceImpl
import org.stepik.android.data.social_profile.repository.SocialProfileRepositoryImpl
import org.stepik.android.data.social_profile.source.SocialProfileCacheDataSource
import org.stepik.android.data.social_profile.source.SocialProfileRemoteDataSource
import org.stepik.android.domain.social_profile.repository.SocialProfileRepository
import org.stepik.android.remote.social_profile.SocialProfileRemoteDataSourceImpl
import org.stepik.android.remote.social_profile.service.SocialProfilesService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class SocialProfileDataModule {
    @Binds
    internal abstract fun bindSocialProfileRepository(
        socialProfileRepositoryImpl: SocialProfileRepositoryImpl
    ): SocialProfileRepository

    @Binds
    internal abstract fun bindSocialProfileRemoteDataSource(
        socialProfileRemoteDataSourceImpl: SocialProfileRemoteDataSourceImpl
    ): SocialProfileRemoteDataSource

    @Binds
    internal abstract fun bindSocialProfileCacheDataSource(
        socialProfileCacheDataSourceImpl: SocialProfileCacheDataSourceImpl
    ): SocialProfileCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideSocialProfileService(@Authorized retrofit: Retrofit): SocialProfilesService =
            retrofit.create(SocialProfilesService::class.java)
    }
}