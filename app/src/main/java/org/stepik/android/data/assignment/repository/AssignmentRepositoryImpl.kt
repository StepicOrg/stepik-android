package org.stepik.android.data.assignment.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.data.assignment.source.AssignmentCacheDataSource
import org.stepik.android.data.assignment.source.AssignmentRemoteDataSource
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Assignment
import javax.inject.Inject

class AssignmentRepositoryImpl
@Inject
constructor(
    private val assignmentRemoteDataSource: AssignmentRemoteDataSource,
    private val assignmentCacheDataSource: AssignmentCacheDataSource
) : AssignmentRepository {
    private val delegate =
        ListRepositoryDelegate(
            assignmentRemoteDataSource::getAssignments,
            assignmentCacheDataSource::getAssignments,
            assignmentCacheDataSource::saveAssignments
        )

    override fun getAssignments(assignmentIds: List<Long>, sourceType: DataSourceType): Single<List<Assignment>> =
        delegate.get(assignmentIds, sourceType, allowFallback = true)

    override fun getAssignmentByUnitAndStep(unitId: Long, stepId: Long): Maybe<Assignment> =
        assignmentCacheDataSource.getAssignmentByUnitAndStep(unitId, stepId)
}