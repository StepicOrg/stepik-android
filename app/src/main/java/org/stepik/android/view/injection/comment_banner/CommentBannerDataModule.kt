package org.stepik.android.view.injection.comment_banner

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.comment_banner.CommentBannerDataCacheSourceImpl
import org.stepik.android.data.comment_banner.repository.CommentBannerRepositoryImpl
import org.stepik.android.data.comment_banner.source.CommentBannerCacheDataSource
import org.stepik.android.domain.comment_banner.repository.CommentBannerRepository

@Module
abstract class CommentBannerDataModule {
    @Binds
    internal abstract fun bindCommentsBannerRepository(
        commentsBannerRepositoryImpl: CommentBannerRepositoryImpl
    ): CommentBannerRepository

    @Binds
    internal abstract fun bindCommentsBannerCacheDataSource(
        commentsBannerCacheDataSourceImpl: CommentBannerDataCacheSourceImpl
    ): CommentBannerCacheDataSource
}