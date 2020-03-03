package org.stepik.android.domain.course.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course

interface CourseRepository {
    fun getCourse(courseId: Long, canUseCache: Boolean = true): Maybe<Course>

    fun getCourses(vararg courseIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<Course>>

    fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>>
}