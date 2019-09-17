package org.stepik.android.view.injection.vote

import dagger.Binds
import dagger.Module
import org.stepik.android.data.vote.repository.VoteRepositoryImpl
import org.stepik.android.data.vote.source.VoteRemoteDataSource
import org.stepik.android.domain.vote.repository.VoteRepository
import org.stepik.android.remote.vote.VoteRemoteDataSourceImpl

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
}