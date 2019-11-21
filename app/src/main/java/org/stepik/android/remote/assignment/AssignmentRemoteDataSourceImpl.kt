package org.stepik.android.remote.assignment

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.assignment.source.AssignmentRemoteDataSource
import org.stepik.android.model.Assignment
import org.stepik.android.remote.assignment.model.AssignmentResponse
import org.stepik.android.remote.assignment.service.AssignmentService
import org.stepik.android.remote.base.chunkedSingleMap
import javax.inject.Inject

class AssignmentRemoteDataSourceImpl
@Inject
constructor(
    private val assigmentService: AssignmentService
) : AssignmentRemoteDataSource {
    private val assignmentResponseMapper =
        Function<AssignmentResponse, List<Assignment>>(AssignmentResponse::assignments)

    override fun getAssignments(vararg assignmentIds: Long): Single<List<Assignment>> =
        assignmentIds
            .chunkedSingleMap { ids ->
                assigmentService.getAssignments(ids)
                    .map(assignmentResponseMapper)
            }
}