package org.stepik.android.data.assignment.repository

import io.reactivex.Single
import org.stepik.android.data.assignment.source.AssignmentCacheDataSource
import org.stepik.android.data.assignment.source.AssignmentRemoteDataSource
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Assignment
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import ru.nobird.android.domain.rx.requireSize
import javax.inject.Inject

class AssignmentRepositoryImpl
@Inject
constructor(
    private val assignmentRemoteDataSource: AssignmentRemoteDataSource,
    private val assignmentCacheDataSource: AssignmentCacheDataSource
) : AssignmentRepository {
    override fun getAssignments(vararg assignmentIds: Long, primarySourceType: DataSourceType): Single<List<Assignment>> {
        val remoteSource = assignmentRemoteDataSource
            .getAssignments(*assignmentIds)
            .doCompletableOnSuccess(assignmentCacheDataSource::saveAssignments)

        val cacheSource = assignmentCacheDataSource
            .getAssignments(*assignmentIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(assignmentIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedAssignments ->
                    val ids = (assignmentIds.toList() - cachedAssignments.map(Assignment::id)).toLongArray()
                    assignmentRemoteDataSource
                        .getAssignments(*ids)
                        .doCompletableOnSuccess(assignmentCacheDataSource::saveAssignments)
                        .map { remoteAssignments -> cachedAssignments + remoteAssignments }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { assignments -> assignments.sortedBy { assignmentIds.indexOf(it.id) } }
    }
}