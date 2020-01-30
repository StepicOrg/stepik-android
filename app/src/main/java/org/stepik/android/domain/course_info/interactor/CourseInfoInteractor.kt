package org.stepik.android.domain.course_info.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class CourseInfoInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,
    private val userRepository: UserRepository,
    private val courseReviewRepository: CourseReviewSummaryRepository
) {
    fun getCourseInfoData(): Observable<CourseInfoData> =
        courseObservableSource
            .flatMap(::getCourseInfoUsers)

    private fun getCourseInfoUsers(course: Course): Observable<CourseInfoData> {
        val emptySource = Observable.just(mapToCourseInfoData(course))

        val instructorsSource = userRepository.getUsers(userIds = *course.instructors ?: longArrayOf())
        val ownerSource = userRepository.getUsers(course.owner)

        val remoteSource =
            zip(instructorsSource, ownerSource, resolveCourseReview(course)) { instructors, owners, review ->
                mapToCourseInfoData(course, instructors, owners.firstOrNull(), review)
            }

        return emptySource
            .concatWith(remoteSource.toObservable())
            .onErrorReturn {
                mapToCourseInfoData(course, instructors = emptyList()) // fallback on network error
            }
    }

    private fun resolveCourseReview(course: Course): Single<Double> =
        if (course.enrollment > 0) {
            courseReviewRepository
                .getCourseReviewSummary(course.reviewSummary, sourceType = DataSourceType.REMOTE)
                .map(CourseReviewSummary::average)
                .toSingle()
                .onErrorReturnItem(0.0)
        } else {
            Single.just(0.0)
        }

    private fun mapToCourseInfoData(course: Course, instructors: List<User>? = null, organization: User? = null, review: Double = 0.0) =
        CourseInfoData(
            organization   = organization?.takeIf(User::isOrganization),
            videoMediaData = course.introVideo
                ?.takeUnless { it.urls.isNullOrEmpty() }
                ?.let { video ->
                    VideoPlayerMediaData(
                        thumbnail = course.cover,
                        title = course.title ?: "",
                        externalVideo = video
                    )
                },
            about          = course.description?.takeIf(String::isNotBlank),
            requirements   = course.requirements?.takeIf(String::isNotBlank),
            targetAudience = course.targetAudience?.takeIf(String::isNotBlank),
            timeToComplete = course.timeToComplete ?: 0,
            instructors    = (instructors ?: course.instructors?.map { null })?.takeIf { it.isNotEmpty() },
            language       = course.language,
            certificate    = course.certificate
                ?.takeIf {
                    val hasText = it.isNotEmpty()
                    val anyCertificateThreshold = course.certificateRegularThreshold > 0 || course.certificateDistinctionThreshold > 0
                    anyCertificateThreshold && (hasText || (course.isCertificateAutoIssued && course.isCertificateIssued))
                }
                ?.let {
                    CourseInfoData.Certificate(
                        title = it,
                        distinctionThreshold = course.certificateDistinctionThreshold,
                        regularThreshold     = course.certificateRegularThreshold
                    )
                },
            stats = if (course.enrollment > 0) CourseStats(review, course.learnersCount, course.readiness) else null
        )
}