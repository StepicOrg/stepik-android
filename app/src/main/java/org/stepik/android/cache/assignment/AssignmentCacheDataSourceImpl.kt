package org.stepik.android.cache.assignment

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.assignment.source.AssignmentCacheDataSource
import org.stepik.android.model.Assignment
import javax.inject.Inject

class AssignmentCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : AssignmentCacheDataSource {
    override fun getAssignments(vararg assignmentIds: Long): Single<List<Assignment>> =
        Single.fromCallable {
            databaseFacade.getAssignments(assignmentIds)
        }

    override fun saveAssignments(assignments: List<Assignment>): Completable =
        Completable.fromAction {
            databaseFacade.addAssignments(assignments)
        }
}