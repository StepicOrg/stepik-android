package org.stepik.android.remote.user_courses

import io.reactivex.Single
import org.stepic.droid.util.PagedList
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

    override fun saveUserCourse(userCourseId: Long, userCourse: UserCourse): Single<PagedList<UserCourse>> =
        userCoursesService
            .saveUserCourse(userCourseId, UserCoursesRequest(userCourse))
            .map { it.toPagedList(UserCoursesResponse::userCourse) }
}