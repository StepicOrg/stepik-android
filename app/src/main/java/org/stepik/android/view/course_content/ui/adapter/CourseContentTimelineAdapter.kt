package org.stepik.android.view.course_content.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_course_content_section_date.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.safeDiv
import org.stepik.android.view.course_content.model.CourseContentSectionDate
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
        private val dateDot = root.dateDot
        private val dateProgress = root.dateProgress
        private val dateTitle = root.dateTitle
        private val dateValue = root.dateValue

        internal fun bind(data: CourseContentSectionDate) {
            dateTitle.setText(data.titleRes)
            dateValue.text = DateTimeHelper.getPrintableDate(data.date, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

            val isNotLastItem = adapterPosition < itemCount - 1
            dateProgress.isVisible = isNotLastItem
            if (isNotLastItem) {
                val total = (dates[adapterPosition + 1].date.time - data.date.time)
                val progress = (now.time - data.date.time) * 100 safeDiv total
                dateProgress.max = 100
                dateProgress.progress = progress.toInt()
            }

            dateDot.isEnabled = now >= data.date
        }
    }
}