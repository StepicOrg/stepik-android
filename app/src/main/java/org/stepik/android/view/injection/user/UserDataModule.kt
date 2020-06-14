package org.stepik.android.view.injection.user

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.user.UserCacheDataSourceImpl
import org.stepik.android.data.user.repository.UserRepositoryImpl
import org.stepik.android.data.user.source.UserCacheDataSource
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.remote.user.UserRemoteDataSourceImpl
import org.stepik.android.remote.user.service.UserService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class UserDataModule {
    @Binds
    internal abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    internal abstract fun bindUserRemoteDataSource(
        userRemoteDataSourceImpl: UserRemoteDataSourceImpl
    ): UserRemoteDataSource

    @Binds
    internal abstract fun bindUserCacheDataSource(
        userCacheDataSourceImpl: UserCacheDataSourceImpl
    ): UserCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideUserService(@Authorized retrofit: Retrofit): UserService =
            retrofit.create(UserService::class.java)
    }
}