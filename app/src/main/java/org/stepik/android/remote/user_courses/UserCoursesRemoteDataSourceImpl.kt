package org.stepik.android.remote.user_courses

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.first
import org.stepic.droid.util.maybeFirst
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.user_courses.model.UserCoursesRequest
import org.stepik.android.remote.user_courses.model.UserCoursesResponse
import org.stepik.android.remote.user_courses.service.UserCoursesService
import javax.inject.Inject

class UserCoursesRemoteDataSourceImpl
@Inject
constructor(
    private val userCoursesService: UserCoursesService
) : UserCoursesRemoteDataSource {
    override fun getUserCourses(page: Int): Single<PagedList<UserCourse>> =
        userCoursesService
            .getUserCourses(page)
            .map { it.toPagedList(UserCoursesResponse::userCourse) }

    override fun getUserCourseByCourseId(courseId: Long): Maybe<UserCourse> =
        userCoursesService
            .getUserCourseByCourseId(courseId)
            .map(UserCoursesResponse::userCourse)
            .maybeFirst()

    override fun saveUserCourse(userCourseId: Long, userCourse: UserCourse): Single<UserCourse> =
        userCoursesService
            .saveUserCourse(userCourseId, UserCoursesRequest(userCourse))
            .map(UserCoursesResponse::userCourse)
            .first()
}