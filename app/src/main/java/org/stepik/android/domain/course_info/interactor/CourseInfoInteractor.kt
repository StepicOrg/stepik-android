package org.stepik.android.domain.course_info.interactor

import io.reactivex.Observable
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class CourseInfoInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,
    private val userRepository: UserRepository
) {
    fun getCourseInfoData(): Observable<CourseInfoData> =
        courseObservableSource
            .flatMap(::getCourseInfoUsers)

    private fun getCourseInfoUsers(course: Course): Observable<CourseInfoData> {
        val emptySource = Observable.just(mapToCourseInfoData(course))

        val authorIds = course.authors ?: emptyList()
        val instructorIds = course.instructors ?: emptyList()

        val remoteSource =
            userRepository.getUsers(userIds = authorIds + instructorIds).map { users ->
                val usersById = users.associateBy(User::id)
                val filteredAuthorIds = authorIds - instructorIds

                val filteredAuthors = filteredAuthorIds.mapNotNull { usersById[it] }
                val instructors = instructorIds.mapNotNull { usersById[it] }
                mapToCourseInfoData(course, filteredAuthors, instructors)
            }

        return emptySource
            .concatWith(remoteSource.toObservable())
            .onErrorReturn {
                mapToCourseInfoData(course, authors = emptyList(), instructors = emptyList()) // fallback on network error
            }
    }

    private fun mapToCourseInfoData(course: Course, authors: List<User>? = null, instructors: List<User>? = null): CourseInfoData =
        CourseInfoData(
            summary        = course.summary?.takeIf(String::isNotBlank),
            authors        = (authors ?: calculateAuthorIds(course).map { null }).takeIf { it.isNotEmpty() },
            acquiredSkills = course.acquiredSkills,
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
            certificate = course.certificate
                ?.takeIf { course.withCertificate }
                ?.let {
                    CourseInfoData.Certificate(
                        title = it,
                        distinctionThreshold = course.certificateDistinctionThreshold,
                        regularThreshold     = course.certificateRegularThreshold
                    )
                },
            learnersCount = course.learnersCount
        )

    private fun calculateAuthorIds(course: Course): List<Long> =
        (course.authors ?: emptyList()) - (course.instructors ?: emptyList())
}