package org.stepik.android.domain.user_courses.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.UserCourse

interface UserCoursesRepository {
    fun getUserCourses(page: Int): Single<PagedList<UserCourse>>
}