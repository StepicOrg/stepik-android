package org.stepik.android.view.injection.assignment

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.assignment.AssignmentCacheDataSourceImpl
import org.stepik.android.data.assignment.repository.AssignmentRepositoryImpl
import org.stepik.android.data.assignment.source.AssignmentCacheDataSource
import org.stepik.android.data.assignment.source.AssignmentRemoteDataSource
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.remote.assignment.AssignmentRemoteDataSourceImpl

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
}