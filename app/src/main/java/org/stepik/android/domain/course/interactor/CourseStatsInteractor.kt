package org.stepik.android.domain.course.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import ru.nobird.android.core.model.mapToLongArray
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import org.stepik.android.model.Progressable
import javax.inject.Inject

class CourseStatsInteractor
@Inject
constructor(
    //    private val billingRepository: BillingRepository,
    private val courseReviewRepository: CourseReviewSummaryRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val progressRepository: ProgressRepository,
    private val userCoursesRepository: UserCoursesRepository
) {

    fun getCourseStats(courses: List<Course>, sourceType: DataSourceType = DataSourceType.REMOTE, resolveEnrollmentState: Boolean = true): Single<List<CourseStats>> =
        zip(
            resolveCourseReview(courses, sourceType),
            resolveCourseProgress(courses, sourceType),
            resolveCoursesEnrollmentStates(courses, sourceType, resolveEnrollmentState)
        ) { courseReviews, courseProgresses, enrollmentStates ->
            val reviewsMap = courseReviews.associateBy(CourseReviewSummary::course)
            val progressMaps = courseProgresses.associateBy(Progress::id)
            val enrollmentMap = enrollmentStates.toMap()

            courses.map { course ->
                CourseStats(
                    review = reviewsMap[course.id]?.average ?: 0.0,
                    learnersCount = course.learnersCount,
                    readiness = course.readiness,
                    progress = course.progress?.let(progressMaps::get),
                    enrollmentState = enrollmentMap.getValue(course.id)
                )
            }
        }

    /**
     * Load course reviews for not enrolled [courses]
     */
    private fun resolveCourseReview(courses: List<Course>, sourceType: DataSourceType): Single<List<CourseReviewSummary>> =
        courseReviewRepository
            .getCourseReviewSummaries(courseReviewSummaryIds = *courses.filter { it.enrollment == 0L }.mapToLongArray { it.reviewSummary }, sourceType = sourceType)
            .onErrorReturnItem(emptyList())

    private fun resolveCourseProgress(courses: List<Course>, sourceType: DataSourceType): Single<List<Progress>> =
        progressRepository
            .getProgresses(progressIds = *courses.mapNotNull(Progressable::progress).toTypedArray(), primarySourceType = sourceType)

    private fun resolveCoursesEnrollmentStates(courses: List<Course>, sourceType: DataSourceType, resolveEnrollmentState: Boolean): Single<List<Pair<Long, EnrollmentState>>> =
        courses
            .toObservable()
            .flatMapSingle { resolveCourseEnrollmentState(it, sourceType, resolveEnrollmentState) }
            .toList()

    private fun resolveCourseEnrollmentState(course: Course, sourceType: DataSourceType, resolveEnrollmentState: Boolean): Single<Pair<Long, EnrollmentState>> =
        when {
            course.enrollment > 0 ->
                userCoursesRepository
                    .getUserCourseByCourseId(course.id, sourceType)
                    .map {
                        course.id to EnrollmentState.Enrolled(it) as EnrollmentState
                    }
                    .toSingle()

            !course.isPaid ->
                Single.just(course.id to EnrollmentState.NotEnrolledFree)

            resolveEnrollmentState ->
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

            else ->
                Single.just(course.id to EnrollmentState.NotEnrolledFree)
        }
}