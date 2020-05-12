package org.stepik.android.domain.course_list.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListInteractor
@Inject
constructor(
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseRepository: CourseRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {

    fun getAllCourses(courseListQuery: CourseListQuery): Single<List<Course>> =
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { courseRepository.getCourses(courseListQuery.copy(page = it)) }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }

    fun getCourseListItems(vararg courseId: Long): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(*courseId))

    fun getCourseListItems(courseListQuery: CourseListQuery): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(courseListQuery))

    private fun getCourseListItems(coursesSource: Single<PagedList<Course>>): Single<PagedList<CourseListItem.Data>> =
        coursesSource
            .flatMap(::obtainCourseListItem)

    private fun obtainCourseListItem(courses: PagedList<Course>): Single<PagedList<CourseListItem.Data>> =
        courseStatsInteractor
            .getCourseStats(courses, resolveEnrollmentState = false)
            .map { courseStats ->
                val list = courses.mapIndexed { index, course ->
                    CourseListItem.Data(
                        course = course,
                        courseStats = courseStats[index],
                        isAdaptive = adaptiveCoursesResolver.isAdaptive(course.id)
                    )
                }
                PagedList(
                    list = list,
                    page = courses.page,
                    hasNext = courses.hasNext,
                    hasPrev = courses.hasPrev
                )
            }
}