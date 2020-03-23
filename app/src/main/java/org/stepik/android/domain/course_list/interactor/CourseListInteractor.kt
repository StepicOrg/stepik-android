package org.stepik.android.domain.course_list.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.tags.repository.TagsRepository
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Tag
import javax.inject.Inject

class CourseListInteractor
@Inject
constructor(
    private val userCoursesRepository: UserCoursesRepository,
    private val courseRepository: CourseRepository,
    private val tagsRepository: TagsRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {

    // TODO Remove this method
    fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseRepository
            .getCourses(courseListQuery)

    // TODO Remove this method
    fun getSavedCourses(courseIds: LongArray): Single<PagedList<Course>> =
        courseRepository
            .getCourses(*courseIds, primarySourceType = DataSourceType.CACHE)

    fun getCourseListItems(vararg courseId: Long): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(*courseId))

    fun getCourseListItems(courseListQuery: CourseListQuery): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(courseListQuery))

    fun getUserCourses(): Single<PagedList<CourseListItem.Data>> =
        userCoursesRepository
            .getUserCourses()
            .flatMap { userCourses ->
                getCourseListItems(*userCourses.mapToLongArray { it.course })
                    .map { courseListItems ->
                        PagedList(list = courseListItems, page = userCourses.page, hasPrev = userCourses.hasPrev, hasNext = userCourses.hasNext)
                    }
            }

    fun getCoursesByTag(tag: Tag): Single<PagedList<CourseListItem.Data>> =
        tagsRepository
            .getSearchResultsOfTag(1, tag, "ru")
            .flatMap { searchResult ->
                getCourseListItems(*searchResult.mapToLongArray { it.course })
                    .map { courseListItems ->
                        PagedList(list = courseListItems, page = searchResult.page, hasPrev = searchResult.hasPrev, hasNext = searchResult.hasNext)
                    }
            }

    private fun getCourseListItems(coursesSource: Single<PagedList<Course>>): Single<PagedList<CourseListItem.Data>> =
        coursesSource
            .flatMap { courses ->
                courses
                    .toObservable()
                    .flatMapMaybe(::obtainCourseListItem)
                    .toList()
                    .map { PagedList(it, courses.page, courses.hasNext, courses.hasPrev) }
            }

    private fun obtainCourseListItem(course: Course): Maybe<CourseListItem.Data> =
        courseStatsInteractor
            .getCourseStats(course)
            .map { courseStats ->
                CourseListItem.Data(
                    course = course,
                    courseStats = courseStats
                )
            }
            .toMaybe()
}