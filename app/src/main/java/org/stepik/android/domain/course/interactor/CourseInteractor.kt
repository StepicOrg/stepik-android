package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.domain.user_courses.model.UserCourseHeader
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val userCoursesRepository: UserCoursesRepository,
    private val solutionsInteractor: SolutionsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>,
    private val courseStatsInteractor: CourseStatsInteractor
) {
    companion object {
        private const val COURSE_TIER_PREFIX = "course_tier_"
    }

    fun getCourseHeaderData(courseId: Long, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(courseId, canUseCache)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap(::obtainCourseHeaderData)

    /**
     * Trying to fetch DB data in first place as course object passed with intent could be obsolete
     */
    fun getCourseHeaderData(course: Course): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(course.id)
            .onErrorReturnItem(course)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap(::obtainCourseHeaderData)

    private fun obtainCourseHeaderData(course: Course): Maybe<CourseHeaderData> =
        zip(
            courseStatsInteractor.getCourseStats(listOf(course)),
            solutionsInteractor.fetchAttemptCacheItems(course.id, localOnly = true),
            obtainUserCourse(course)
        ) { courseStats, localSubmissions, userCourseHeader ->
            CourseHeaderData(
                courseId = course.id,
                course = course,
                userCourseHeader = userCourseHeader,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = courseStats.first(),
                localSubmissionsCount = localSubmissions.count { it is SolutionItem.SubmissionItem }
            )
        }
            .toMaybe()

    private fun obtainUserCourse(course: Course): Single<UserCourseHeader> =
        if (course.enrollment != 0L) {
            userCoursesRepository
                .getUserCourseByCourseId(course.id)
                .map { UserCourseHeader.Data(userCourse = it, isSending = false) as UserCourseHeader }
                .toSingle()
                .onErrorReturnItem(UserCourseHeader.Empty)
        } else {
            Single.just(UserCourseHeader.Empty)
        }
}