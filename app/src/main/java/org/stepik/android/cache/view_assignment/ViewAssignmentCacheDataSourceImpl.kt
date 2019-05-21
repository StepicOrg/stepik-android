package org.stepik.android.cache.view_assignment

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.view_assignment.source.ViewAssignmentCacheDataSource
import org.stepik.android.model.ViewAssignment
import javax.inject.Inject

class ViewAssignmentCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : ViewAssignmentCacheDataSource {
    override fun createViewAssignment(viewAssignment: ViewAssignment): Completable =
        Completable.fromAction {
            databaseFacade.addToQueueViewedState(viewAssignment)
        }

    override fun getViewAssignments(): Single<List<ViewAssignment>> =
        Single
            .fromCallable(databaseFacade::getAllInQueue)

    override fun removeViewAssignment(viewAssignment: ViewAssignment): Completable =
        Completable.fromAction {
            databaseFacade.removeFromQueue(viewAssignment)
        }
}