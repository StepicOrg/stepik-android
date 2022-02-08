package org.stepik.android.view.course_news.ui.adapter.delegate

import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseNewsBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.announcement.model.Announcement
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

            val mustShowStatistics = isTeacher &&
                (data.announcement.status == Announcement.AnnouncementStatus.SENDING ||
                    data.announcement.status == Announcement.AnnouncementStatus.SENT)

            val teacherInformation =
                if (mustShowStatistics) {
                    buildSpannedString {
                        data.announcement.publishCount?.let {
                            appendCount(this, R.string.course_news_publish_count, it)
                        }
                        data.announcement.queueCount?.let {
                            appendCount(this, R.string.course_news_queued_count, it)
                        }
                        data.announcement.sentCount?.let {
                            appendCount(this, R.string.course_news_sent_count, it)
                        }
                        data.announcement.openCount?.let {
                            appendCount(this, R.string.course_news_open_count, it)
                        }
                        data.announcement.clickCount?.let {
                            appendCount(this, R.string.course_news_click_count, it, newline = false)
                        }
                    }
                } else {
                    ""
                }

            viewBinding.newsStatistics.text = teacherInformation
            viewBinding.newsStatistics.isVisible = teacherInformation.isNotEmpty()
        }

        private fun appendCount(spannableStringBuilder: SpannableStringBuilder, stringRes: Int, count: Int, newline: Boolean = true) {
            with(spannableStringBuilder) {
                append(context.getString(stringRes))
                bold { append(count.toString()) }
                if (newline) append("\n")
            }
        }
    }
}