package org.stepik.android.remote.view_assignment

import io.reactivex.Completable
import org.stepik.android.data.view_assignment.source.ViewAssignmentRemoteDataSource
import org.stepik.android.model.ViewAssignment
import org.stepik.android.remote.view_assignment.model.ViewAssignmentRequest
import org.stepik.android.remote.view_assignment.service.ViewAssignmentService
import javax.inject.Inject

class ViewAssignmentRemoteDataSourceImpl
@Inject
constructor(
    private val viewAssignmentService: ViewAssignmentService
) : ViewAssignmentRemoteDataSource  {
    override fun createViewAssignment(viewAssignment: ViewAssignment): Completable =
        viewAssignmentService.postViewedReactive(ViewAssignmentRequest(viewAssignment))
}