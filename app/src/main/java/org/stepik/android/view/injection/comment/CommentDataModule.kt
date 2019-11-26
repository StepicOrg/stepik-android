package org.stepik.android.view.injection.comment

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.comment.repository.CommentRepositoryImpl
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.remote.comment.CommentRemoteDataSourceImpl
import org.stepik.android.remote.comment.service.CommentService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCommentService(@Authorized retrofit: Retrofit): CommentService =
            retrofit.create(CommentService::class.java)
    }
}