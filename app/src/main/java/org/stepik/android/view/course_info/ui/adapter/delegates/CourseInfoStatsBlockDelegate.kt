package org.stepik.android.view.course_info.ui.adapter.delegates

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.layout_course_stats.view.*
import org.stepic.droid.R
import org.stepik.android.view.course.ui.delegates.CourseStatsDelegate
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate

class CourseInfoStatsBlockDelegate :  AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup): CourseInfoAdapter.ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_stats_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.StatsBlock

    private class ViewHolder(root: View) : CourseInfoAdapter.ViewHolder(root) {
        private val statsDelegate = CourseStatsDelegate(root)

        init {
            @ColorInt
            val accentColor = ContextCompat.getColor(context, R.color.new_accent_color)

            with(root.courseLearnersCount) {
                setTextColor(accentColor)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(accentColor))
            }

            root.courseFeatured.setTextColor(accentColor)
            with(root.courseRating) {
                secondaryProgressRes = R.drawable.ic_progress_star_white_filled
                backgroundRes = R.drawable.ic_progress_star_white_filled
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.StatsBlock
            statsDelegate.setStats(data.courseStats)
        }
    }
}