package org.stepic.droid.storage.dao

import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepik.android.model.Course

interface CourseListDao {
    fun addCourseList(courseListType: DbStructureCourseList.Type, courses: List<Course>)
    fun getCourseList(courseListType: DbStructureCourseList.Type): List<Course>
    fun removeCourseList(courseListType: DbStructureCourseList.Type)
    fun removeCourseFromList(courseListType: DbStructureCourseList.Type, courseId: Long)
    fun removeAll()
}