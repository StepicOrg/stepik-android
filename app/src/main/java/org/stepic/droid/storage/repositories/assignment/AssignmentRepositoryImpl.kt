package org.stepic.droid.storage.repositories.assignment

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.progress.ProgressRepository
import org.stepic.droid.web.Api
import javax.inject.Inject

class AssignmentRepositoryImpl
@Inject
constructor(
        private val api: Api,
        private val databaseFacade: DatabaseFacade,
        private val progressRepository: ProgressRepository
) : AssignmentRepository {
    override fun syncAssignments(vararg assignmentIds: Long): Completable = Single
            .fromCallable { api.getAssignments(assignmentIds).execute().body()?.assignments!! }
            .doOnSuccess { assignments -> assignments.forEach { databaseFacade.addAssignment(it) } }
            .flatMapCompletable { progressRepository.syncProgresses(*it.toTypedArray()) }
}