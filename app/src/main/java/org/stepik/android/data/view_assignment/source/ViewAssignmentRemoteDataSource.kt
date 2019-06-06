package org.stepik.android.data.view_assignment.source

import io.reactivex.Completable
import org.stepik.android.model.ViewAssignment

interface ViewAssignmentRemoteDataSource {
    fun createViewAssignment(viewAssignment: ViewAssignment): Completable
}