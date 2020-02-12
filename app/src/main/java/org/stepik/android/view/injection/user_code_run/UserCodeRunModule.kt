package org.stepik.android.view.injection.user_code_run

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.user_code_run.repository.UserCodeRunRepositoryImpl
import org.stepik.android.data.user_code_run.source.UserCodeRunRemoteDataSource
import org.stepik.android.domain.user_code_run.repository.UserCodeRunRepository
import org.stepik.android.remote.user_code_run.UserCodeRunRemoteDataSourceImpl
import org.stepik.android.remote.user_code_run.service.UserCodeRunService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class UserCodeRunModule {
    @Binds
    internal abstract fun bindUserCodeRunRepository(
        userCodeRunRepositoryImpl: UserCodeRunRepositoryImpl
    ): UserCodeRunRepository

    @Binds
    internal abstract fun bindUserCodeRemoteDataSource(
        userCodeRunRemoteDataSourceImpl: UserCodeRunRemoteDataSourceImpl
    ): UserCodeRunRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideUserRunCodeService(@Authorized retrofit: Retrofit): UserCodeRunService =
            retrofit.create(UserCodeRunService::class.java)
    }
}