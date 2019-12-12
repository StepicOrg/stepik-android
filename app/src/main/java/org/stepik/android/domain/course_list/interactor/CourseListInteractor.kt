package org.stepik.android.domain.course_list.interactor

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListInteractor
@Inject
constructor(
    private val courseListRepository: CourseListRepository
) {
    fun getCourseList(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseListRepository
            .getCourseList(courseListQuery)
}