package org.stepik.android.domain.course_list.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.then
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseListUserInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val userCoursesRepository: UserCoursesRepository,
    private val courseListInteractor: CourseListInteractor
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

    fun getAllUserCourses(): Single<List<UserCourse>> =
        requireAuthorization then
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { userCoursesRepository.getUserCourses(page = it, sourceType = DataSourceType.REMOTE) }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }

    fun getCourseListItems(vararg courseId: Long): Single<PagedList<CourseListItem.Data>> =
        courseListInteractor.getCourseListItems(*courseId)

    fun getUserCourse(courseId: Long): Single<CourseListItem.Data> =
        courseListInteractor
            .getCourseListItems(courseId)
            .map { it.first() }
}