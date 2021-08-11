package org.stepik.android.view.course_list.resolver

import android.content.Context
import org.stepic.droid.R
import javax.inject.Inject

class TableLayoutHorizontalSpanCountResolver
@Inject
constructor(
    private val context: Context
) {
    companion object {
        private const val SINGLE_ROW_SPAN_COUNT = 1
    }

    fun resolveSpanCount(courseListSize: Int): Int =
        if (courseListSize <= context.resources.getInteger(R.integer.course_list_columns)) {
            SINGLE_ROW_SPAN_COUNT
        } else {
            context.resources.getInteger(R.integer.course_list_rows)
        }
}