package org.stepik.android.data.course.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import ru.nobird.android.domain.rx.maybeFirst
import ru.nobird.android.domain.rx.requireSize
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseRepositoryImpl
@Inject
constructor(
    private val courseRemoteDataSource: CourseRemoteDataSource,
    private val courseCacheDataSource: CourseCacheDataSource
) : CourseRepository {

    override fun getCourse(courseId: Long, canUseCache: Boolean): Maybe<Course> {
        val remoteSource = courseRemoteDataSource.getCourses(courseId).maybeFirst()
            .doCompletableOnSuccess(courseCacheDataSource::saveCourse)

        val cacheSource = courseCacheDataSource.getCourses(courseId).maybeFirst()

        return if (canUseCache) {
            cacheSource.switchIfEmpty(remoteSource)
        } else {
            remoteSource
        }
    }

    override fun getCourses(vararg courseIds: Long, primarySourceType: DataSourceType): Single<PagedList<Course>> {
        if (courseIds.isEmpty()) return Single.just(PagedList(emptyList()))

        val remoteSource = courseRemoteDataSource
            .getCourses(*courseIds)
            .doCompletableOnSuccess(courseCacheDataSource::saveCourses)

        val cacheSource = courseCacheDataSource
            .getCourses(*courseIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(courseIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedCourses ->
                    val ids = (courseIds.toList() - cachedCourses.map(Course::id)).toLongArray()
                    courseRemoteDataSource
                        .getCourses(*ids)
                        .doCompletableOnSuccess(courseCacheDataSource::saveCourses)
                        .map { remoteCourses -> cachedCourses + remoteCourses }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { courses -> PagedList(courses.sortedBy { courseIds.indexOf(it.id) }) }
    }

    override fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseRemoteDataSource
            .getCourses(courseListQuery)
            .doCompletableOnSuccess(courseCacheDataSource::saveCourses)

    override fun removeCachedCourses(): Completable =
        courseCacheDataSource
            .removeCachedCourses()
}