package org.stepik.android.domain.course.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import javax.inject.Inject

class CourseStatsInteractor
@Inject
constructor(
    //    private val billingRepository: BillingRepository,
    private val courseReviewRepository: CourseReviewSummaryRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val progressRepository: ProgressRepository
) {

    fun getCourseStats(courses: List<Course>): Single<List<CourseStats>> =
        zip(
            resolveCourseReview(courses),
            resolveCourseProgress(courses),
            resolveCoursesEnrollmentStates(courses)
        ) { courseReviews, courseProgresses, enrollmentStates ->
            val reviewsMap = courseReviews.associateBy(CourseReviewSummary::course)
            val progressMaps = courseProgresses.associateBy(Progress::id)
            val enrollmentMap = enrollmentStates.toMap()

            courses.map { course ->
                CourseStats(
                    review = reviewsMap[course.id]?.average ?: 0.0,
                    learnersCount = course.learnersCount,
                    readiness = course.readiness,
                    progress = course.progress?.let { progressMaps[it] },
                    enrollmentState = enrollmentMap.getValue(course.id)
                )
            }
        }

    private fun resolveCourseReview(courses: List<Course>): Single<List<CourseReviewSummary>> =
        courseReviewRepository
            .getCourseReviewSummaries(courseReviewSummaryIds = *courses.mapToLongArray { it.reviewSummary }, sourceType = DataSourceType.REMOTE)
            .onErrorReturnItem(emptyList())

    private fun resolveCourseProgress(courses: List<Course>): Single<List<Progress>> =
        progressRepository
            .getProgresses(
                progressIds = *courses.mapNotNull { it.progress }.toTypedArray()
            )

    private fun resolveCoursesEnrollmentStates(courses: List<Course>): Single<List<Pair<Long, EnrollmentState>>> =
        courses.toObservable().flatMapSingle { resolveCourseEnrollmentState(it) }.toList()

    private fun resolveCourseEnrollmentState(course: Course): Single<Pair<Long, EnrollmentState>> =
        when {
            course.enrollment > 0 ->
                Single.just(course.id to EnrollmentState.Enrolled)

            !course.isPaid ->
                Single.just(course.id to EnrollmentState.NotEnrolledFree)

            else ->
                coursePaymentsRepository
                    .getCoursePaymentsByCourseId(course.id, coursePaymentStatus = CoursePayment.Status.SUCCESS)
                    .flatMap { payments ->
                        if (payments.isEmpty()) {
                            Single.just(course.id to EnrollmentState.NotEnrolledWeb)
//                            billingRepository
//                                .getInventory(ProductTypes.IN_APP, COURSE_TIER_PREFIX + course.priceTier)
//                                .map(::SkuSerializableWrapper)
//                                .map(EnrollmentState::NotEnrolledInApp)
//                                .cast(EnrollmentState::class.java)
//                                .toSingle(EnrollmentState.NotEnrolledWeb) // if price_tier == null
                        } else {
                            Single.just(course.id to EnrollmentState.NotEnrolledFree)
                        }
                    }
                    .onErrorReturnItem(course.id to EnrollmentState.NotEnrolledWeb) // if billing not supported on current device or to access paid course offline
        }
}