package org.stepik.android.view.injection.vote

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.vote.repository.VoteRepositoryImpl
import org.stepik.android.data.vote.source.VoteRemoteDataSource
import org.stepik.android.domain.vote.repository.VoteRepository
import org.stepik.android.remote.vote.VoteRemoteDataSourceImpl
import org.stepik.android.remote.vote.service.VoteService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
internal abstract class VoteDataModule {
    @Binds
    internal abstract fun bindVoteRepository(
        voteRepositoryImpl: VoteRepositoryImpl
    ): VoteRepository

    @Binds
    internal abstract fun bindVoteRemoteDataSource(
        voteRemoteDataSourceImpl: VoteRemoteDataSourceImpl
    ): VoteRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideVoteService(@Authorized retrofit: Retrofit): VoteService =
            retrofit.create(VoteService::class.java)
    }
}