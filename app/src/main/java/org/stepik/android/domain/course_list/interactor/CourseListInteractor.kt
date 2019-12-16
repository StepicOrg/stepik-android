package org.stepik.android.domain.course_list.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.plus
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
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { page ->
                courseListRepository
                    .getCourseList(courseListQuery.copy(page = page))
            }
            .takeUntil { !it.hasNext }
            .reduce(PagedList(emptyList())) { a, b -> a + b }
}