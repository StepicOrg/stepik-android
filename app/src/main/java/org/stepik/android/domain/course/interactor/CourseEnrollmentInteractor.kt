package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.model.CourseListType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseEnrollmentInteractor
@Inject
constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,

    private val courseRepository: CourseRepository,
    private val courseListRepository: CourseListRepository,

    private val joiningPoster: JoiningPoster,
    private val droppingPoster: DroppingPoster,

    private val deadlinesRepository: DeadlinesRepository,
    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>
) {
    companion object {
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
    }

    private val requireAuthorization: Completable =
        Completable.create { emitter ->
            if (sharedPreferenceHelper.authResponseFromStore != null) {
                emitter.onComplete()
            } else {
                emitter.onError(UNAUTHORIZED_EXCEPTION_STUB)
            }
        }

    fun enrollCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .addEnrollment(courseId)
            .andThen(courseListRepository.addCourseToList(CourseListType.ENROLLED, courseId))
            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
            .doOnSuccess(joiningPoster::joinCourse) // interop with old code
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    fun dropCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .removeEnrollment(courseId)
            .andThen(deadlinesRepository.removeDeadlineRecordByCourseId(courseId).onErrorComplete())
            .andThen(courseListRepository.removeCourseFromList(CourseListType.ENROLLED, courseId))
            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
            .doOnSuccess(droppingPoster::successDropCourse) // interop with old code
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes
}