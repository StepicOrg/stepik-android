package org.stepik.android.data.assignment.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.model.Assignment

interface AssignmentCacheDataSource {
    fun getAssignment(assignmentId: Long): Maybe<Assignment> =
        getAssignments(assignmentId).maybeFirst()

    fun getAssignments(vararg assignmentIds: Long): Single<List<Assignment>>

    fun saveAssignment(assignment: Assignment): Completable =
        saveAssignments(listOf(assignment))

    fun saveAssignments(assignments: List<Assignment>): Completable
}