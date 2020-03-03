package org.stepik.android.domain.course.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
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

    fun getCourseStats(course: Course): Single<CourseStats> =
        zip(
            resolveCourseReview(course),
            resolveCourseProgress(course),
            resolveCourseEnrollmentState(course)
        ) { courseReview, courseProgress, enrollmentState ->
            CourseStats(
                review = courseReview,
                learnersCount = course.learnersCount,
                readiness = course.readiness,
                progress = (courseProgress as? Progress),
                enrollmentState = enrollmentState
            )
        }

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
}