package org.stepik.android.view.injection.discussion_proxy

import dagger.Binds
import dagger.Module
import org.stepik.android.data.discussion_proxy.repository.DiscussionProxyRepositoryImpl
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.domain.discussion_proxy.repository.DiscussionProxyRepository
import org.stepik.android.remote.discussion_proxy.DiscussionProxyRemoteDataSourceImpl

@Module
internal abstract class DiscussionProxyDataModule {
    @Binds
    internal abstract fun bindDiscussionProxyRepository(
        discussionProxyRepositoryImpl: DiscussionProxyRepositoryImpl
    ): DiscussionProxyRepository

    @Binds
    internal abstract fun bindDiscussionProxyRemoteDataSource(
        discussionProxyRemoteDataSourceImpl: DiscussionProxyRemoteDataSourceImpl
    ): DiscussionProxyRemoteDataSource
}