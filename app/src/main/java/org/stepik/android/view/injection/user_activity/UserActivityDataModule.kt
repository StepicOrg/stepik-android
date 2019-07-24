package org.stepik.android.view.injection.user_activity

import dagger.Binds
import dagger.Module
import org.stepik.android.data.user_activity.repository.UserActivityRepositoryImpl
import org.stepik.android.data.user_activity.source.UserActivityRemoteDataSource
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.remote.user_activity.UserActivityRemoteDataSourceImpl

@Module
abstract class UserActivityDataModule {
    @Binds
    internal abstract fun bindUserActivityRepository(
        userActivityRepositoryImpl: UserActivityRepositoryImpl
    ): UserActivityRepository

    @Binds
    internal abstract fun bindUserActivityRemoteDataSource(
        userActivityRemoteDataSourceImpl: UserActivityRemoteDataSourceImpl
    ): UserActivityRemoteDataSource
}