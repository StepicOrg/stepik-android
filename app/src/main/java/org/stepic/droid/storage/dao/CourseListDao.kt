package org.stepic.droid.storage.dao

import org.stepic.droid.model.CourseListType
import org.stepik.android.model.Course

interface CourseListDao {
    fun addCourseList(courseListType: CourseListType, courses: List<Course>)
    fun getCourseList(courseListType: CourseListType): List<Course>
    fun removeCourseList(courseListType: CourseListType)
    fun removeCourseFromList(courseListType: CourseListType, courseId: Long)
    fun removeAll()
}