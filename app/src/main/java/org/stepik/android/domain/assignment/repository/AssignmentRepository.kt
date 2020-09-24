package org.stepik.android.domain.assignment.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Assignment

interface AssignmentRepository {
    fun getAssignments(assignmentIds: List<Long>, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Assignment>>
}