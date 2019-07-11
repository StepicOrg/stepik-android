package org.stepik.android.view.injection.comment

import dagger.Binds
import dagger.Module
import org.stepik.android.data.comment.repository.CommentRepositoryImpl
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.remote.comment.CommentRemoteDataSourceImpl

@Module
internal abstract class CommentDataModule {
    @Binds
    internal abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    internal abstract fun bindCommentRemoteDataSource(
        commentRemoteDataSourceImpl: CommentRemoteDataSourceImpl
    ): CommentRemoteDataSource
}