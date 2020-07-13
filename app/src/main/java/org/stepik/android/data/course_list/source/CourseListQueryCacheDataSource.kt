package org.stepik.android.data.course_list.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_list.model.CourseListQuery

interface CourseListQueryCacheDataSource {
    fun getCourses(courseListQuery: CourseListQuery): Single<List<Long>>
    fun saveCourses(courseListQuery: CourseListQuery, courses: LongArray): Completable
}