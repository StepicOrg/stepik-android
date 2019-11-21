package org.stepik.android.view.injection.view_assignment

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.view_assignment.ViewAssignmentCacheDataSourceImpl
import org.stepik.android.data.view_assignment.repository.ViewAssignmentRepositoryImpl
import org.stepik.android.data.view_assignment.source.ViewAssignmentCacheDataSource
import org.stepik.android.data.view_assignment.source.ViewAssignmentRemoteDataSource
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.remote.view_assignment.ViewAssignmentRemoteDataSourceImpl
import org.stepik.android.remote.view_assignment.service.ViewAssignmentService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideViewAssignmentService(@Authorized retrofit: Retrofit): ViewAssignmentService =
            retrofit.create(ViewAssignmentService::class.java)
    }
}