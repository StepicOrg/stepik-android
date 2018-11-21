package org.stepik.android.domain.course_info.interactor

import io.reactivex.Observable
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.util.concat
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import javax.inject.Inject

class CourseInfoInteractor
@Inject
constructor(
    private val courseObservableSource: BehaviorSubject<Course>,
    private val userRepository: UserRepository
) {
    fun getCourseInfoData(): Observable<CourseInfoData> =
            courseObservableSource.concatMap(::getCourseInfoUsers)

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
    }

    private fun mapToCourseInfoData(course: Course, instructors: List<User>? = null, organization: User? = null) =
        CourseInfoData(
            organization   = organization?.fullName,
            video          = course.introVideo,
            about          = course.description?.takeIf(String::isNotBlank),
            requirements   = course.requirements?.takeIf(String::isNotBlank),
            targetAudience = course.targetAudience?.takeIf(String::isNotBlank),
            timeToComplete = course.timeToComplete ?: 0,
            instructors    = instructors?.takeIf { it.isNotEmpty() } ?: course.instructors?.map { null },
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