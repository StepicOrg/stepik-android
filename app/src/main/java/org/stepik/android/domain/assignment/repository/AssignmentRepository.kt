package org.stepik.android.domain.assignment.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Assignment

interface AssignmentRepository {
    fun getAssignment(assignmentId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Assignment> =
        getAssignments(assignmentId, primarySourceType = primarySourceType).maybeFirst()

    fun getAssignments(vararg assignmentIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Assignment>>
}