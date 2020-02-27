package org.stepik.android.domain.course_list.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListExperimentalInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val courseListRepository: CourseListRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {

    fun getCourseListItems(vararg courseId: Long): Single<PagedList<CourseListItem>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(*courseId))

    fun getCourseListItems(courseListQuery: CourseListQuery): Single<PagedList<CourseListItem>> =
        getCourseListItems(coursesSource = courseListRepository.getCourseList(courseListQuery))

    private fun getCourseListItems(coursesSource: Single<PagedList<Course>>): Single<PagedList<CourseListItem>> =
        coursesSource
            .flatMap { courses ->
                courses
                    .toObservable()
                    .flatMapMaybe(::obtainCourseListItem)
                    .toList()
                    .map { PagedList(it, courses.page, courses.hasNext, courses.hasPrev) }
            }

    private fun obtainCourseListItem(course: Course): Maybe<CourseListItem> =
        courseStatsInteractor.getCourseStats(course).map { courseStats ->
            CourseListItem(
                course = course,
                courseStats = courseStats
            )
        }
            .toMaybe()
}