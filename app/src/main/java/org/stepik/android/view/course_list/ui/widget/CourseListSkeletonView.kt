package org.stepik.android.view.course_list.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate

class CourseListSkeletonView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {
    init {
        orientation = HORIZONTAL
        val columnsCount = resources.getInteger(R.integer.course_list_columns)

        weightSum = columnsCount.toFloat()

        for (i in 0 until columnsCount) {
            val skeletonView = inflate(R.layout.item_course_list_skeleton, attachToRoot = false)
            skeletonView.updateLayoutParams<LayoutParams> {
                weight = 1f
            }
            addView(skeletonView)
        }
    }
}