package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val solutionsInteractor: SolutionsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>,
    private val courseDataResolverInteractor: CourseDataResolverInteractor
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
            courseDataResolverInteractor.resolveCourseReview(course),
            courseDataResolverInteractor.resolveCourseProgress(course),
            courseDataResolverInteractor.resolveCourseEnrollmentState(course),
            solutionsInteractor.fetchAttemptCacheItems(course.id, localOnly = true)
        ) { courseReview, courseProgress, enrollmentState, localSubmissions ->
            CourseHeaderData(
                courseId = course.id,
                course = course,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = CourseStats(courseReview, course.learnersCount, course.readiness),
                progress = (courseProgress as? Progress),
                localSubmissionsCount = localSubmissions.count { it is SolutionItem.SubmissionItem },
                enrollmentState = enrollmentState
            )
        }
            .toMaybe()

    fun restoreCourse(course: Course) {
        coursePublishSubject.onNext(course)
    }
}