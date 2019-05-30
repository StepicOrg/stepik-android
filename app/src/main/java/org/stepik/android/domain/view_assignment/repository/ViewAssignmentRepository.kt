package org.stepik.android.domain.view_assignment.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.ViewAssignment

interface ViewAssignmentRepository {
    /**
     * Tries to create [viewAssignment] at [dataSourceType]
     */
    fun createViewAssignment(viewAssignment: ViewAssignment, dataSourceType: DataSourceType = DataSourceType.REMOTE): Completable

    /**
     * Returns local view assignments queue
     */
    fun getViewAssignments(): Single<List<ViewAssignment>>

    /**
     * Remove local [viewAssignment]
     */
    fun removeViewAssignment(viewAssignment: ViewAssignment): Completable
}