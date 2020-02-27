package org.stepik.android.domain.course.interactor

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import javax.inject.Inject

class CourseDataResolverInteractor
@Inject
constructor(
    //    private val billingRepository: BillingRepository,
    private val courseReviewRepository: CourseReviewSummaryRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val progressRepository: ProgressRepository
) {

    fun resolveCourseReview(course: Course): Single<Double> =
        courseReviewRepository
            .getCourseReviewSummary(course.reviewSummary, sourceType = DataSourceType.REMOTE)
            .map(CourseReviewSummary::average)
            .toSingle()
            .onErrorReturnItem(0.0)

    fun resolveCourseProgress(course: Course): Single<*> =
        course
            .progress
            ?.let(progressRepository::getProgress)
            ?: Single.just(Unit)

    fun resolveCourseEnrollmentState(course: Course): Single<EnrollmentState> =
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