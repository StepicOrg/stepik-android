package org.stepik.android.presentation.course_list.delegate

import io.reactivex.Single
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListView

interface CourseListPresenterDelegate {
    fun onCourseListQuery(courseListQuery: CourseListQuery): Single<CourseListView.State>
    fun onCourseIds(vararg courseIds: Long): Single<CourseListView.State>
}