package org.stepik.android.data.user_courses.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import javax.inject.Inject

class UserCoursesRepositoryImpl
@Inject
constructor(
    private val userCoursesRemoteDataSource: UserCoursesRemoteDataSource,
    private val userCoursesCacheDataSource: UserCoursesCacheDataSource
) : UserCoursesRepository {
    override fun getUserCourses(userCourseQuery: UserCourseQuery, sourceType: DataSourceType): Single<PagedList<UserCourse>> {
        val remoteSource = userCoursesRemoteDataSource
            .getUserCourses(userCourseQuery)
            .doCompletableOnSuccess(userCoursesCacheDataSource::saveUserCourses)

        val cacheSource = userCoursesCacheDataSource
            .getUserCourses(userCourseQuery)
            .map { PagedList(it) }

        return when (sourceType) {
            DataSourceType.CACHE ->
                cacheSource

            DataSourceType.REMOTE ->
                if (userCourseQuery.page == 1) {
                    remoteSource.onErrorResumeNext(cacheSource)
                } else {
                    remoteSource
                }
        }
    }

    override fun saveUserCourse(userCourse: UserCourse): Single<UserCourse> =
        userCoursesRemoteDataSource
            .saveUserCourse(userCourse.id, userCourse)
            .doCompletableOnSuccess { userCoursesCacheDataSource.saveUserCourses(listOf(it)) }

    override fun getUserCourseByCourseId(courseId: Long): Maybe<UserCourse> =
        userCoursesRemoteDataSource.getUserCourseByCourseId(courseId)

    override fun addUserCourse(userCourse: UserCourse): Completable =
        userCoursesCacheDataSource.saveUserCourses(listOf(userCourse))

    override fun removeUserCourse(courseId: Long): Completable =
        userCoursesCacheDataSource.removeUserCourse(courseId)
}