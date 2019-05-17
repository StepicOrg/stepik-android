package org.stepik.android.data.assignment.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Assignment

interface AssignmentRemoteDataSource {
    fun getAssignment(assignmentId: Long): Maybe<Assignment> =
        getAssignments(assignmentId).maybeFirst()

    fun getAssignments(vararg assignmentIds: Long): Single<List<Assignment>>
}