package org.stepik.android.data.user_courses.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.UserCourse

interface UserCoursesRemoteDataSource {
    fun getUserCourses(page: Int): Single<PagedList<UserCourse>>
}