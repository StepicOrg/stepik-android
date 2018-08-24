package org.stepic.droid.storage.repositories.assignment

import io.reactivex.Completable

interface AssignmentRepository {
    fun syncAssignments(vararg assignmentIds: Long): Completable
}