package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.domain.user_courses.interactor.UserCoursesInteractor
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.model.Course
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.WishlistOperationBus
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
    private val lessonRepository: LessonRepository,

    private val deadlinesRepository: DeadlinesRepository,
    private val wishlistRepository: WishlistRepository,
    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<WishlistOperationData>,
    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>,

    private val userCoursesInteractor: UserCoursesInteractor
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

    fun fetchCourseEnrollmentAfterPurchaseInWeb(courseId: Long): Completable =
        requireAuthorization then
        courseRepository
            .getCourse(courseId, sourceType = DataSourceType.REMOTE, allowFallback = false)
            .toSingle()
            .flatMapCompletable { course ->
                if (course.enrollment > 0) {
                    userCoursesInteractor.addUserCourse(course.id)
                        .andThen(lessonRepository.removeCachedLessons(course.id))
                        .doOnComplete { enrollmentSubject.onNext(course) }
                } else {
                    Completable.complete()
                }
            }

    fun enrollCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .addEnrollment(courseId)
            .andThen(userCoursesInteractor.addUserCourse(courseId))
            .andThen(lessonRepository.removeCachedLessons(courseId))
            .andThen(removeCourseFromWishlist(courseId))
            .andThen(courseRepository.getCourse(courseId, sourceType = DataSourceType.REMOTE, allowFallback = false).toSingle())
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    fun dropCourse(courseId: Long): Single<Course> =
        requireAuthorization then
        enrollmentRepository
            .removeEnrollment(courseId)
            .andThen(deadlinesRepository.removeDeadlineRecordByCourseId(courseId).onErrorComplete())
            .andThen(userCoursesInteractor.removeUserCourse(courseId))
            .andThen(lessonRepository.removeCachedLessons(courseId))
            .andThen(courseRepository.getCourse(courseId, sourceType = DataSourceType.REMOTE, allowFallback = false).toSingle())
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    private fun removeCourseFromWishlist(courseId: Long): Completable =
        wishlistRepository
            .removeCourseFromWishlist(courseId)
            .andThen(
                Completable.fromAction {
                    wishlistOperationPublisher.onNext(WishlistOperationData(courseId, WishlistAction.REMOVE))
                }
            )
}