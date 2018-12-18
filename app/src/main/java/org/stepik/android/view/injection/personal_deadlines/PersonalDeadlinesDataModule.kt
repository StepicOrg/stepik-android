package org.stepik.android.view.injection.personal_deadlines

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.personal_deadlines.DeadlinesBannerCacheDataSourceImpl
import org.stepik.android.cache.personal_deadlines.DeadlinesCacheDataSourceImpl
import org.stepik.android.data.personal_deadlines.repository.DeadlinesBannerRepositoryImpl
import org.stepik.android.data.personal_deadlines.repository.DeadlinesRepositoryImpl
import org.stepik.android.data.personal_deadlines.source.DeadlinesBannerCacheDataSource
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepik.android.data.personal_deadlines.source.DeadlinesRemoteDataSource
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesBannerRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.domain.personal_deadlines.resolver.DeadlinesResolver
import org.stepik.android.domain.personal_deadlines.resolver.DeadlinesResolverImpl
import org.stepik.android.remote.personal_deadlines.DeadlinesRemoteDataSourceImpl

@Module
abstract class PersonalDeadlinesDataModule {
    @Binds
    internal abstract fun bindDeadlinesRepository(
        deadlinesRepositoryImpl: DeadlinesRepositoryImpl
    ): DeadlinesRepository

    @Binds
    internal abstract fun bindDeadlinesRemoteDataSource(
        deadlinesRemoteDataSourceImpl: DeadlinesRemoteDataSourceImpl
    ): DeadlinesRemoteDataSource

    @Binds
    internal abstract fun bindDeadlinesCacheDataSource(
        deadlinesCacheDataSourceImpl: DeadlinesCacheDataSourceImpl
    ): DeadlinesCacheDataSource

    @Binds
    internal abstract fun bindDeadlinesResolver(
        deadlinesResolverImpl: DeadlinesResolverImpl
    ): DeadlinesResolver


    @Binds
    internal abstract fun bindDeadlinesBannerRepository(
        deadlinesBannerRepositoryImpl: DeadlinesBannerRepositoryImpl
    ): DeadlinesBannerRepository

    @Binds
    internal abstract fun bindDeadlinesBannerCacheDataSource(
        deadlinesBannerCacheDataSourceImpl: DeadlinesBannerCacheDataSourceImpl
    ): DeadlinesBannerCacheDataSource
}