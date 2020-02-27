package org.stepik.android.domain.course_list.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.plus
import org.stepik.android.domain.course.interactor.CourseDataResolverInteractor
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import javax.inject.Inject

class CourseListExperimentalInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val courseListRepository: CourseListRepository,
    private val courseDataResolverInteractor: CourseDataResolverInteractor
) {

    fun getCourseListItems(vararg courseId: Long): Single<List<CourseListItem>> =
        courseRepository
            .getCourses(*courseId)
            .flatMap { courses ->
                courses
                    .toObservable()
                    .flatMapSingle { course -> obtainCourseListItem(course = course).toSingle() }
                    .reduce(emptyList<CourseListItem>()) { a, b -> a + b }
            }

    fun getCourseListItems(courseListQuery: CourseListQuery, page: Int = 1): Single<PagedList<CourseListItem>> =
        courseListRepository
            .getCourseList(courseListQuery.copy(page = page))
            .flatMap { courses ->
                courses
                    .toObservable()
                    .flatMapSingle { course -> obtainCourseListItem(course = course).toSingle() }
                    .reduce(PagedList<CourseListItem>(emptyList())) { a, b -> a + b }
            }

    private fun obtainCourseListItem(course: Course): Maybe<CourseListItem> =
        Singles.zip(
            courseDataResolverInteractor.resolveCourseReview(course),
            courseDataResolverInteractor.resolveCourseProgress(course),
            courseDataResolverInteractor.resolveCourseEnrollmentState(course)
        ) { courseReview, courseProgress, enrollmentState ->
            CourseListItem(
                courseId = course.id,
                course = course,
                progress = (courseProgress as? Progress),
                rating = courseReview,
                enrollmentState = enrollmentState
            )
        }
            .toMaybe()
}