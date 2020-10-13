package org.stepik.android.data.assignment.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Assignment

interface AssignmentCacheDataSource {
    fun getAssignments(assignmentIds: List<Long>): Single<List<Assignment>>

    fun getAssignmentByUnitAndStep(
        unitId: Long,
        stepId: Long
    ): Maybe<Assignment>

    fun saveAssignments(assignments: List<Assignment>): Completable
}