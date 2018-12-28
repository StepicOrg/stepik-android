package org.stepik.android.data.course_list.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CourseListType
import org.stepik.android.model.Course

interface CourseListCacheDataSource {
    fun getCourseList(courseListType: CourseListType): Single<List<Course>>

    fun addCourseToList(courseListType: CourseListType, courseId: Long): Completable
    fun removeCourseFromList(courseListType: CourseListType, courseId: Long): Completable
}