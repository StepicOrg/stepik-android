package org.stepik.android.view.injection.user_profile

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.user_profile.source.UserProfileRemoteDataSource
import org.stepik.android.remote.user_profile.UserProfileRemoteDataSourceImpl
import org.stepik.android.remote.user_profile.service.UserProfileService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class UserProfileDataModule {
    @Binds
    internal abstract fun bindUserProfileRemoteDataSource(
        userProfileRemoteDataSourceImpl: UserProfileRemoteDataSourceImpl
    ): UserProfileRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideUserProfileService(@Authorized retrofit: Retrofit): UserProfileService =
            retrofit.create(UserProfileService::class.java)
    }
}