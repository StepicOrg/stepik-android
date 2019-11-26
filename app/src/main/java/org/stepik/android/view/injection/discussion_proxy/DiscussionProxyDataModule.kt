package org.stepik.android.view.injection.discussion_proxy

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.discussion_proxy.repository.DiscussionProxyRepositoryImpl
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.domain.discussion_proxy.repository.DiscussionProxyRepository
import org.stepik.android.remote.discussion_proxy.DiscussionProxyRemoteDataSourceImpl
import org.stepik.android.remote.discussion_proxy.service.DiscussionProxyService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideDiscussionProxyService(@Authorized retrofit: Retrofit): DiscussionProxyService =
            retrofit.create(DiscussionProxyService::class.java)
    }
}