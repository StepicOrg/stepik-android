package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
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

    private val deadlinesRepository: DeadlinesRepository,
    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Long>
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

    fun enrollCourse(courseId: Long): Completable =
        requireAuthorization then
        enrollmentRepository
            .addEnrollment(courseId)
            .doOnComplete { enrollmentSubject.onNext(courseId) } // notify everyone about changes

    fun dropCourse(courseId: Long): Completable =
        requireAuthorization then
        enrollmentRepository
            .removeEnrollment(courseId)
            .andThen(deadlinesRepository.removeDeadlineRecordByCourseId(courseId).onErrorComplete())
            .doOnComplete { enrollmentSubject.onNext(courseId) } // notify everyone about changes
}