package org.stepik.android.data.view_assignment.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.view_assignment.source.ViewAssignmentCacheDataSource
import org.stepik.android.data.view_assignment.source.ViewAssignmentRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.model.ViewAssignment
import javax.inject.Inject

class ViewAssignmentRepositoryImpl
@Inject
constructor(
    private val viewAssignmentRemoteDataSource: ViewAssignmentRemoteDataSource,
    private val viewAssignmentCacheDataSource: ViewAssignmentCacheDataSource
) : ViewAssignmentRepository {
    override fun createViewAssignment(viewAssignment: ViewAssignment, dataSourceType: DataSourceType): Completable =
        when (dataSourceType) {
            DataSourceType.REMOTE ->
                viewAssignmentRemoteDataSource
                    .createViewAssignment(viewAssignment)

            DataSourceType.CACHE ->
                viewAssignmentCacheDataSource
                    .createViewAssignment(viewAssignment)

            else ->
                throw IllegalArgumentException("Unsupported source type = $dataSourceType")
        }

    override fun getViewAssignments(): Single<List<ViewAssignment>> =
        viewAssignmentCacheDataSource
            .getViewAssignments()

    override fun removeViewAssignment(viewAssignment: ViewAssignment): Completable =
        viewAssignmentCacheDataSource
            .removeViewAssignment(viewAssignment)
}