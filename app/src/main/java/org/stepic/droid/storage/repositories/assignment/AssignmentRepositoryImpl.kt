package org.stepic.droid.storage.repositories.assignment

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.getProgresses
import org.stepic.droid.web.Api
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Assignment
import org.stepik.android.remote.base.chunkedSingleMap
import javax.inject.Inject

class AssignmentRepositoryImpl
@Inject
constructor(
    private val api: Api,
    private val databaseFacade: DatabaseFacade,
    private val progressRepository: ProgressRepository
) : AssignmentRepository {
    override fun syncAssignments(vararg assignmentIds: Long): Completable =
        assignmentIds
            .chunkedSingleMap { getAssignments(it) }
            .doOnSuccess { assignments -> assignments.forEach { databaseFacade.addAssignment(it) } }
            .flatMapCompletable { progressRepository.getProgresses(*it.getProgresses()).toCompletable() }

    private fun getAssignments(ids: LongArray): Single<List<Assignment>> =
        Single.fromCallable {
            api.getAssignments(ids).execute().body()?.assignments
        }
}