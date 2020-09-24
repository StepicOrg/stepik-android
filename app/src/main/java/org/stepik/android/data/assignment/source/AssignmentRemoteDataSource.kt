package org.stepik.android.data.assignment.source

import io.reactivex.Single
import org.stepik.android.model.Assignment

interface AssignmentRemoteDataSource {
    fun getAssignments(assignmentIds: List<Long>): Single<List<Assignment>>
}