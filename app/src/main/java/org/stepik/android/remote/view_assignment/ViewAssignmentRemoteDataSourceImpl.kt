package org.stepik.android.remote.view_assignment

import io.reactivex.Completable
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.view_assignment.source.ViewAssignmentRemoteDataSource
import org.stepik.android.model.ViewAssignment
import org.stepik.android.remote.view_assignment.model.ViewAssignmentRequest
import javax.inject.Inject

class ViewAssignmentRemoteDataSourceImpl
@Inject
constructor(
    private val loggedService: StepicRestLoggedService
) : ViewAssignmentRemoteDataSource  {
    override fun createViewAssignment(viewAssignment: ViewAssignment): Completable =
        loggedService.postViewedReactive(ViewAssignmentRequest(viewAssignment))
}