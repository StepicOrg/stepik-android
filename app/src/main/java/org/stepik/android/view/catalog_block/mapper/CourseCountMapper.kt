package org.stepik.android.view.catalog_block.mapper

import android.content.Context
import org.stepic.droid.R
import javax.inject.Inject

class CourseCountMapper
@Inject
constructor() {
    companion object {
        private const val MAX_COURSE_COUNT = 99
    }

    fun mapCourseCountToString(context: Context, count: Int): String =
        if (count > MAX_COURSE_COUNT) {
            context.getString(R.string.courses_max_count)
        } else {
            context.resources.getQuantityString(R.plurals.course_count, count, count)
        }
}