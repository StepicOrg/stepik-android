package org.stepik.android.view.injection.discussion_thread

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.discussion_thread.DiscussionThreadCacheDataSourceImpl
import org.stepik.android.data.discussion_thread.repository.DiscussionThreadRepositoryImpl
import org.stepik.android.data.discussion_thread.source.DiscussionThreadCacheDataSource
import org.stepik.android.data.discussion_thread.source.DiscussionThreadRemoteDataSource
import org.stepik.android.domain.discussion_thread.repository.DiscussionThreadRepository
import org.stepik.android.remote.discussion_thread.DiscussionThreadRemoteDataSourceImpl

@Module
abstract class DiscussionThreadDataModule {
    @Binds
    internal abstract fun bindDiscussionThreadRepository(
        discussion_threadRepositoryImpl: DiscussionThreadRepositoryImpl
    ): DiscussionThreadRepository

    @Binds
    internal abstract fun bindDiscussionThreadCacheDataSource(
        discussion_threadCacheDataSourceImpl: DiscussionThreadCacheDataSourceImpl
    ): DiscussionThreadCacheDataSource

    @Binds
    internal abstract fun bindDiscussionThreadRemoteDataSource(
        discussion_threadRemoteDataSourceImpl: DiscussionThreadRemoteDataSourceImpl
    ): DiscussionThreadRemoteDataSource
}