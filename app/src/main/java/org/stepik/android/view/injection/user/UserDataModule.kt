package org.stepik.android.view.injection.user

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.user.UserCacheDataSourceImpl
import org.stepik.android.data.user.repository.UserRepositoryImpl
import org.stepik.android.data.user.source.UserCacheDataSource
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.remote.user.UserRemoteDataSourceImpl

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
}