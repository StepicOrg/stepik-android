package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.attempts.interactor.AttemptsInteractor
import org.stepik.android.domain.attempts.model.AttemptCacheItem
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
//    private val billingRepository: BillingRepository,
    private val courseRepository: CourseRepository,
    private val courseReviewRepository: CourseReviewSummaryRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val progressRepository: ProgressRepository,
    private val attemptsInteractor: AttemptsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>
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
            resolveCourseReview(course),
            resolveCourseProgress(course),
            resolveCourseEnrollmentState(course),
            attemptsInteractor.fetchAttemptCacheItems(course.id, localOnly = true)
        ) { courseReview, courseProgress, enrollmentState, localSubmissions ->
            CourseHeaderData(
                courseId = course.id,
                course = course,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = CourseStats(courseReview, course.learnersCount, course.readiness),
                progress = (courseProgress as? Progress),
                localSubmissionsCount = localSubmissions.count { it is AttemptCacheItem.SubmissionItem },
                enrollmentState = enrollmentState
            )
        }
            .toMaybe()

    private fun resolveCourseReview(course: Course): Single<Double> =
        courseReviewRepository
            .getCourseReviewSummary(course.reviewSummary, sourceType = DataSourceType.REMOTE)
            .map(CourseReviewSummary::average)
            .toSingle()
            .onErrorReturnItem(0.0)

    private fun resolveCourseProgress(course: Course): Single<*> =
        course
            .progress
            ?.let(progressRepository::getProgress)
            ?: Single.just(Unit)

    private fun resolveCourseEnrollmentState(course: Course): Single<EnrollmentState> =
        when {
            course.enrollment > 0 ->
                Single.just(EnrollmentState.Enrolled)

            !course.isPaid ->
                Single.just(EnrollmentState.NotEnrolledFree)

            else ->
                coursePaymentsRepository
                    .getCoursePaymentsByCourseId(course.id, coursePaymentStatus = CoursePayment.Status.SUCCESS)
                    .flatMap { payments ->
                        if (payments.isEmpty()) {
                            Single.just(EnrollmentState.NotEnrolledWeb)
//                            billingRepository
//                                .getInventory(ProductTypes.IN_APP, COURSE_TIER_PREFIX + course.priceTier)
//                                .map(::SkuSerializableWrapper)
//                                .map(EnrollmentState::NotEnrolledInApp)
//                                .cast(EnrollmentState::class.java)
//                                .toSingle(EnrollmentState.NotEnrolledWeb) // if price_tier == null
                        } else {
                            Single.just(EnrollmentState.NotEnrolledFree)
                        }
                    }
                    .onErrorReturnItem(EnrollmentState.NotEnrolledWeb) // if billing not supported on current device or to access paid course offline
        }

    fun restoreCourse(course: Course) {
        coursePublishSubject.onNext(course)
    }
}