package org.stepik.android.data.user_courses.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class UserCoursesRepositoryImpl
@Inject
constructor(
    private val userCoursesRemoteDataSource: UserCoursesRemoteDataSource,
    private val userCoursesCacheDataSource: UserCoursesCacheDataSource
) : UserCoursesRepository {

    sealed class Either {
        class Error(val exception: Throwable) : Either()
        class Success(val data: List<UserCourse>) : Either()
    }

    private val userCoursesObservable: BehaviorRelay<Either> = BehaviorRelay.create()

    override fun getUserCoursesShared(): Single<List<UserCourse>> =
        userCoursesObservable
            .firstOrError()
            .flatMap { either ->
                when (either) {
                    is Either.Error -> {
                        Single.error(either.exception)
                    }
                    is Either.Success -> {
                        Single.just(either.data)
                    }
                }
            }

    override fun getUserCoursesShared(userCourseQuery: UserCourseQuery, sourceType: DataSourceType): Single<List<UserCourse>> =
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { getUserCourses(userCourseQuery.copy(page = it), sourceType = sourceType) }
            .takeUntil { !it.hasNext }
            .reduce(emptyList<UserCourse>()) { a, b -> a + b }
            .doOnError {
                userCoursesObservable.accept(Either.Error(it))
            }
            .doAfterSuccess {
                userCoursesObservable.accept(Either.Success(it))
            }

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

    override fun getUserCourseByCourseId(courseId: Long, sourceType: DataSourceType): Maybe<UserCourse> {
        val query = UserCourseQuery(course = courseId)

        val remoteSource = userCoursesRemoteDataSource
            .getUserCourses(query)
            .doCompletableOnSuccess(userCoursesCacheDataSource::saveUserCourses)
            .maybeFirst()

        val cacheSource = userCoursesCacheDataSource
            .getUserCourses(query)
            .maybeFirst()

        return when (sourceType) {
            DataSourceType.CACHE ->
                cacheSource
                    .switchIfEmpty(remoteSource)

            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheSource)
        }
    }

    override fun saveUserCourse(userCourse: UserCourse): Single<UserCourse> =
        userCoursesRemoteDataSource
            .saveUserCourse(userCourse.id, userCourse)
            .doCompletableOnSuccess { userCoursesCacheDataSource.saveUserCourses(listOf(it)) }

    override fun addUserCourse(userCourse: UserCourse): Completable =
        userCoursesCacheDataSource.saveUserCourses(listOf(userCourse))

    override fun removeUserCourse(courseId: Long): Completable =
        userCoursesCacheDataSource.removeUserCourse(courseId)
}