package org.stepik.android.data.view_assignment.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.ViewAssignment

interface ViewAssignmentCacheDataSource {
    fun createViewAssignment(viewAssignment: ViewAssignment): Completable

    fun getViewAssignments(): Single<List<ViewAssignment>>

    fun removeViewAssignment(viewAssignment: ViewAssignment): Completable
}