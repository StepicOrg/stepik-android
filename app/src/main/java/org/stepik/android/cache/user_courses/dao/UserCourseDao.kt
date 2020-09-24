package org.stepik.android.cache.user_courses.dao

import org.stepic.droid.storage.dao.IDao
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCourseDao : IDao<UserCourse> {
    fun getAllUserCourses(whereArgs: Map<String, String>): List<UserCourse>
}