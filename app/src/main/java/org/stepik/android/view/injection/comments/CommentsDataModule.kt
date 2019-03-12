package org.stepik.android.view.injection.comments

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.comments.CommentsBannerDataCacheSourceImpl
import org.stepik.android.data.comments.repository.CommentsBannerRepositoryImpl
import org.stepik.android.data.comments.source.CommentsBannerCacheDataSource
import org.stepik.android.domain.comments.repository.CommentsBannerRepository

@Module
abstract class CommentsDataModule {
    @Binds
    internal abstract fun bindCommentsBannerRepository(
        commentsBannerRepositoryImpl: CommentsBannerRepositoryImpl
    ): CommentsBannerRepository

    @Binds
    internal abstract fun bindCommentsBannerCacheDataSource(
        commentsBannerCacheDataSourceImpl: CommentsBannerDataCacheSourceImpl
    ): CommentsBannerCacheDataSource
}