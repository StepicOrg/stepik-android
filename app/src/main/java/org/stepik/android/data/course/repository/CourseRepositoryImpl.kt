package org.stepik.android.data.course.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.data.course_list.source.CourseListQueryCacheDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import ru.nobird.android.core.model.mapToLongArray
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CourseRepositoryImpl
@Inject
constructor(
    private val courseRemoteDataSource: CourseRemoteDataSource,
    private val courseCacheDataSource: CourseCacheDataSource,
    private val courseListQueryCacheDataSource: CourseListQueryCacheDataSource
) : CourseRepository {
    private val delegate =
        ListRepositoryDelegate(
            courseRemoteDataSource::getCourses,
            courseCacheDataSource::getCourses,
            courseCacheDataSource::saveCourses
        )

    override fun getCourses(vararg courseIds: Long, primarySourceType: DataSourceType, allowFallback: Boolean): Single<PagedList<Course>> =
        delegate
            .get(courseIds.toList(), primarySourceType, allowFallback)
            .map(::PagedList)

    override fun getCourses(courseListQuery: CourseListQuery, primarySourceType: DataSourceType, allowFallback: Boolean): Single<PagedList<Course>> {
        val remoteSource = courseRemoteDataSource
            .getCourses(courseListQuery)
            .doCompletableOnSuccess(courseCacheDataSource::saveCourses)
            .doCompletableOnSuccess { courseListQueryCacheDataSource.saveCourses(courseListQuery, it.mapToLongArray(Course::id)) }

        val cacheSource = courseListQueryCacheDataSource
            .getCourses(courseListQuery)
            .flatMap { ids ->
                getCourses(*ids, primarySourceType = primarySourceType)
            }

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                if (allowFallback) {
                    remoteSource.onErrorResumeNext(cacheSource)
                } else {
                    remoteSource
                }

            DataSourceType.CACHE ->
                if (allowFallback) {
                    cacheSource
                        .filter(Collection<*>::isNotEmpty)
                        .switchIfEmpty(remoteSource)
                } else {
                    cacheSource
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

    override fun removeCachedCourses(): Completable =
        courseCacheDataSource
            .removeCachedCourses()
}