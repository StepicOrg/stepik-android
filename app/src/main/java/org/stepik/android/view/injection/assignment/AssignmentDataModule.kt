package org.stepik.android.view.injection.assignment

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.assignment.AssignmentCacheDataSourceImpl
import org.stepik.android.data.assignment.repository.AssignmentRepositoryImpl
import org.stepik.android.data.assignment.source.AssignmentCacheDataSource
import org.stepik.android.data.assignment.source.AssignmentRemoteDataSource
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.remote.assignment.AssignmentRemoteDataSourceImpl
import org.stepik.android.remote.assignment.service.AssignmentService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class AssignmentDataModule {
    @Binds
    internal abstract fun bindAssignmentRepository(
        assignmentRepositoryImpl: AssignmentRepositoryImpl
    ): AssignmentRepository

    @Binds
    internal abstract fun bindAssignmentCacheDataSource(
        assignmentCacheDataSourceImpl: AssignmentCacheDataSourceImpl
    ): AssignmentCacheDataSource

    @Binds
    internal abstract fun bindAssignmentRemoteDataSource(
        assignmentRemoteDataSourceImpl: AssignmentRemoteDataSourceImpl
    ): AssignmentRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAssignmentService(@Authorized retrofit: Retrofit): AssignmentService =
            retrofit.create(AssignmentService::class.java)
    }
}