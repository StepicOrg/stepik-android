package org.stepik.android.view.course_news.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseNewsBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_news.model.CourseNewsListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.TimeZone

class CourseNewsAdapterDelegate(
    val isTeacher: Boolean
) : AdapterDelegate<CourseNewsListItem, DelegateViewHolder<CourseNewsListItem>>() {
    override fun isForViewType(position: Int, data: CourseNewsListItem): Boolean =
        data is CourseNewsListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseNewsListItem> =
        ViewHolder(createView(parent, R.layout.item_course_news))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseNewsListItem>(root) {
        private val viewBinding: ItemCourseNewsBinding by viewBinding { ItemCourseNewsBinding.bind(root) }

        override fun onBind(data: CourseNewsListItem) {
            data as CourseNewsListItem.Data

            val formattedDate = data.announcement.sentDate?.let { DateTimeHelper.getPrintableDate(it, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()) }
            viewBinding.newsDate.text = formattedDate
            viewBinding.newsDate.isVisible = formattedDate != null

            viewBinding.newsSubject.text = data.announcement.subject
            viewBinding.newsText.setText(data.announcement.text)
        }
    }
}