package org.stepik.android.data.user_courses.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.UserCourse
import javax.inject.Inject

class UserCoursesRepositoryImpl
@Inject
constructor(
    private val userCoursesRemoteDataSource: UserCoursesRemoteDataSource
) : UserCoursesRepository {
    override fun getUserCourses(page: Int): Single<PagedList<UserCourse>> =
        userCoursesRemoteDataSource.getUserCourses(page)
}