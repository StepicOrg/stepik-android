package org.stepik.android.domain.course.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import ru.nobird.android.domain.rx.maybeFirst

interface CourseRepository {
    fun getCourse(courseId: Long, sourceType: DataSourceType = DataSourceType.CACHE, allowFallback: Boolean = true): Maybe<Course> =
        getCourses(courseId, primarySourceType = sourceType, allowFallback = allowFallback)
            .maybeFirst()

    fun getCourses(vararg courseIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE, allowFallback: Boolean = true): Single<PagedList<Course>>

    /**
     * Fetches courses from remote source with [courseListQuery]
     */
    fun getCourses(courseListQuery: CourseListQuery, primarySourceType: DataSourceType = DataSourceType.CACHE, allowFallback: Boolean): Single<PagedList<Course>>

    fun removeCachedCourses(): Completable
}