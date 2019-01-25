package org.stepik.android.domain.course_info.interactor

import io.reactivex.Observable
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.concat
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class CourseInfoInteractor
@Inject
constructor(
    private val config: Config,
    private val courseObservableSource: Observable<Course>,
    private val userRepository: UserRepository
) {
    fun getCourseInfoData(): Observable<CourseInfoData> =
        courseObservableSource
            .take(1)
            .flatMap(::getCourseInfoUsers)

    private fun getCourseInfoUsers(course: Course): Observable<CourseInfoData> {
        val emptySource = Observable.just(mapToCourseInfoData(course))

        val instructorsSource = userRepository.getUsers(userIds = *course.instructors ?: longArrayOf())
        val ownerSource = userRepository.getUsers(course.owner)

        return emptySource concat
                zip(instructorsSource, ownerSource)
                    .toObservable()
                    .map { (instructors, owners) ->
                        mapToCourseInfoData(course, instructors, owners.firstOrNull())
                    }
                    .onErrorReturn {
                        mapToCourseInfoData(course, instructors = emptyList()) // fallback on network error
                    }
    }

    private fun mapToCourseInfoData(course: Course, instructors: List<User>? = null, organization: User? = null) =
        CourseInfoData(
            organization   = organization?.takeIf(User::isOrganization),
            videoMediaData = course.introVideo
                ?.takeUnless { it.urls.isNullOrEmpty() }
                ?.let { video ->
                    VideoPlayerMediaData(
                        thumbnail = config.baseUrl + course.cover,
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
                ?.takeIf { course.isCertificateAutoIssued }
                ?.let {
                    CourseInfoData.Certificate(
                        title = it,
                        distinctionThreshold = course.certificateDistinctionThreshold,
                        regularThreshold     = course.certificateRegularThreshold
                    )
                }
        )
}