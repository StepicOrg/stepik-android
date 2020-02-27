package org.stepik.android.data.user_courses.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.UserCourse
import javax.inject.Inject

class UserCoursesRepositoryImpl
@Inject
constructor(
    private val userCoursesRemoteDataSource: UserCoursesRemoteDataSource,
    private val userCoursesCacheDataSource: UserCoursesCacheDataSource
) : UserCoursesRepository {
    override fun getUserCourses(page: Int, sourceType: DataSourceType): Single<PagedList<UserCourse>> =
        when (sourceType) {
            DataSourceType.CACHE ->
                userCoursesCacheDataSource
                    .getUserCourses()
                    .map { PagedList(it) }

            DataSourceType.REMOTE ->
                userCoursesRemoteDataSource
                    .getUserCourses(page)
                    .doCompletableOnSuccess(userCoursesCacheDataSource::saveUserCourses)
        }
}