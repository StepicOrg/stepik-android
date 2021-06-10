package org.stepik.android.domain.course_complete.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificate.repository.CertificateRepository
import org.stepik.android.domain.course_complete.model.CourseCompleteInfo
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Certificate
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import javax.inject.Inject

class CourseCompleteInteractor
@Inject
constructor(
    private val userPreferences: UserPreferences,
    private val progressRepository: ProgressRepository,
    private val certificateRepository: CertificateRepository,
    private val courseReviewsRepository: CourseReviewsRepository
) {
    fun getCourseCompleteInfo(course: Course): Single<CourseCompleteInfo> =
        Single
            .fromCallable { userPreferences.userId }
            .flatMap { userId ->
                Single.zip(
                    resolveCourseProgress(listOf(course)),
                    resolveCourseCertificate(course, userId),
                    resolveCourseReview(course, userId)

                ) { progresses, certificates, courseReviews ->
                    CourseCompleteInfo(
                        course = course,
                        courseProgress = progresses.first(),
                        certificate = certificates.firstOrNull(),
                        hasReview = courseReviews.isNotEmpty()
                    )
                }
            }

    private fun resolveCourseProgress(courses: List<Course>): Single<List<Progress>> =
        progressRepository
            .getProgresses(courses.getProgresses())
            .onErrorReturnItem(emptyList())

    private fun resolveCourseCertificate(course: Course, userId: Long): Single<List<Certificate>> =
        certificateRepository
            .getCertificate(userId, course.id, sourceType = DataSourceType.REMOTE)
            .map { listOf(it) }
            .toSingle()
            .onErrorReturnItem(emptyList())

    private fun resolveCourseReview(course: Course, userId: Long): Single<List<CourseReview>> =
        courseReviewsRepository
            .getCourseReviewByCourseIdAndUserId(course.id, userId)
            .map { listOf(it) }
            .toSingle()
            .onErrorReturnItem(emptyList())
}