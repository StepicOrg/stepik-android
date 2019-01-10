package org.stepic.droid.util

import android.support.annotation.MenuRes
import org.stepic.droid.R
import org.stepik.android.model.Course

object ContextMenuCourseUtil {
    @MenuRes
    fun getMenuResource(course: Course): Int =
            if (course.enrollment != 0L) {
                R.menu.course_context_menu
            } else {
                R.menu.course_context_not_enrolled_menu
            }
}
