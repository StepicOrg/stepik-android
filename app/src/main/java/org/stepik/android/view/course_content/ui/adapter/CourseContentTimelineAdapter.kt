package org.stepik.android.view.course_content.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseContentSectionDateBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.safeDiv
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import ru.nobird.android.view.base.ui.extension.inflate
import java.util.Date
import java.util.TimeZone

class CourseContentTimelineAdapter : RecyclerView.Adapter<CourseContentTimelineAdapter.ViewHolder>() {
    var dates: List<CourseContentSectionDate> = emptyList()
        set(value) {
            field = value.sortedBy { it.date }
            notifyDataSetChanged()
        }

    val now: Date = Date()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.view_course_content_section_date, false))

    override fun getItemCount(): Int =
        dates.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val viewBinding: ViewCourseContentSectionDateBinding by viewBinding { ViewCourseContentSectionDateBinding.bind(root) }

        internal fun bind(data: CourseContentSectionDate) {
            viewBinding.dateTitle.setText(data.titleRes)
            viewBinding.dateValue.text = DateTimeHelper.getPrintableDate(data.date, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

            val isNotLastItem = adapterPosition < itemCount - 1
            viewBinding.dateProgress.isVisible = isNotLastItem
            if (isNotLastItem) {
                val total = (dates[adapterPosition + 1].date.time - data.date.time)
                val progress = (now.time - data.date.time) * 100 safeDiv total
                viewBinding.dateProgress.max = 100
                viewBinding.dateProgress.progress = progress.toInt()
            }

            viewBinding.dateDot.isEnabled = now >= data.date
        }
    }
}
