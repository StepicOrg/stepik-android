package org.stepik.android.view.injection.view_assignment

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.view_assignment.ViewAssignmentCacheDataSourceImpl
import org.stepik.android.data.view_assignment.repository.ViewAssignmentRepositoryImpl
import org.stepik.android.data.view_assignment.source.ViewAssignmentCacheDataSource
import org.stepik.android.data.view_assignment.source.ViewAssignmentRemoteDataSource
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.remote.view_assignment.ViewAssignmentRemoteDataSourceImpl

@Module
abstract class ViewAssignmentDataModule {
    @Binds
    internal abstract fun bindViewAssignmentRepository(
        viewAssignmentRepositoryImpl: ViewAssignmentRepositoryImpl
    ): ViewAssignmentRepository

    @Binds
    internal abstract fun bindViewAssignmentCacheDataSource(
        viewAssignmentCacheDataSourceImpl: ViewAssignmentCacheDataSourceImpl
    ): ViewAssignmentCacheDataSource

    @Binds
    internal abstract fun bindViewAssignmentRemoteDataSource(
        viewAssignmentRemoteDataSourceImpl: ViewAssignmentRemoteDataSourceImpl
    ): ViewAssignmentRemoteDataSource
}