package org.stepik.android.view.course_news.ui.adapter.delegate

import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemAnnouncementBadgeBinding
import org.stepic.droid.databinding.ItemCourseNewsBinding
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.domain.course_news.model.CourseNewsListItem
import org.stepik.android.view.course_news.model.AnnouncementBadge
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import java.util.Date
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
        private val badgesAdapter = DefaultDelegateAdapter<AnnouncementBadge>()

        init {
            badgesAdapter += adapterDelegate(
                layoutResId = R.layout.item_announcement_badge
            ) {
                val badgesBinding: ItemAnnouncementBadgeBinding = ItemAnnouncementBadgeBinding.bind(this.itemView)
                onBind {
                    val textColor = ContextCompat.getColor(context, it.textColorRes)
                    badgesBinding.root.setText(it.textRes)
                    badgesBinding.root.setTextColor(textColor)
                    badgesBinding.root.setBackgroundResource(it.backgroundRes)
                    badgesBinding.root.setCompoundDrawables(start = it.compoundDrawableRes)
                }
            }
        }

        override fun onBind(data: CourseNewsListItem) {
            data as CourseNewsListItem.Data

            val isOneTimeEvent = !data.announcement.isInfinite && !data.announcement.onEnroll
            val isActiveEvent = data.announcement.onEnroll ||
                (data.announcement.isInfinite && (data.announcement.startDate == null || data.announcement.startDate.time < DateTimeHelper.nowUtc()))

            with(viewBinding.newsBadges) {
                itemAnimator = null
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = badgesAdapter
            }

            viewBinding.newsBadges.isVisible = isTeacher
            badgesAdapter.items = buildBadgesList(data.announcement, isOneTimeEvent, isActiveEvent)

            viewBinding.newsDate.text = formatAnnouncementDate(data.announcement, isActiveEvent)

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

        private fun buildBadgesList(announcement: Announcement, isOneTimeEvent: Boolean, isActiveEvent: Boolean): List<AnnouncementBadge> {
            val statusBadge =
                when (announcement.status) {
                    Announcement.AnnouncementStatus.COMPOSING ->
                        AnnouncementBadge.COMPOSING

                    Announcement.AnnouncementStatus.SCHEDULED ->
                        if (isActiveEvent) {
                            AnnouncementBadge.SENDING
                        } else {
                            AnnouncementBadge.SCHEDULED
                        }

                    Announcement.AnnouncementStatus.QUEUEING,
                    Announcement.AnnouncementStatus.QUEUED,
                    Announcement.AnnouncementStatus.SENDING ->
                        AnnouncementBadge.SENDING

                    Announcement.AnnouncementStatus.SENT,
                    Announcement.AnnouncementStatus.ABORTED ->
                        AnnouncementBadge.SENT
                }

            val eventBadge =
                if (isOneTimeEvent) {
                    AnnouncementBadge.ONE_TIME
                } else {
                    AnnouncementBadge.ON_EVENT
                }

            return listOf(statusBadge, eventBadge)
        }

        private fun formatAnnouncementDate(announcement: Announcement, isActiveEvent: Boolean): String {
            val defaultDate = (announcement.sentDate ?: announcement.createDate) ?: Date()

            val formattedDate =
                if (!isTeacher) {
                    if (isActiveEvent && announcement.noticeDates.lastOrNull() != null) {
                        formatDateTimePattern(announcement.noticeDates.last())
                    } else {
                        null
                    }
                } else {
                    when (announcement.status) {
                        Announcement.AnnouncementStatus.COMPOSING -> {
                            announcement.displayedStartDate?.let { displayedStartDate ->
                                val formattedStartDate = formatDateTimePattern(displayedStartDate)
                                if (isActiveEvent) {
                                    context.getString(
                                        R.string.course_news_on_event_composing,
                                        formattedStartDate
                                    )
                                } else {
                                    formattedStartDate
                                }
                            }
                        }
                        Announcement.AnnouncementStatus.SCHEDULED -> {
                            announcement.displayedStartDate?.let { displayedStartDate ->
                                val formattedStartDate = formatDateTimePattern(displayedStartDate)
                                if (isActiveEvent) {
                                    context.getString(
                                        R.string.course_news_on_event_sending,
                                        formattedStartDate
                                    )
                                } else {
                                    context.getString(
                                        R.string.course_news_one_time_scheduled,
                                        formattedStartDate
                                    )
                                }
                            }
                        }
                        Announcement.AnnouncementStatus.QUEUEING,
                        Announcement.AnnouncementStatus.QUEUED,
                        Announcement.AnnouncementStatus.SENDING -> {
                            announcement.displayedStartDate?.let { displayedStartDate ->
                                val formattedStartDate = formatDateTimePattern(displayedStartDate)
                                if (isActiveEvent) {
                                    context.getString(
                                        R.string.course_news_on_event_sending,
                                        formattedStartDate
                                    )
                                } else {
                                    context.getString(
                                        R.string.course_news_one_time_sending,
                                        formattedStartDate
                                    )
                                }
                            }
                        }
                        Announcement.AnnouncementStatus.SENT,
                        Announcement.AnnouncementStatus.ABORTED -> {
                            announcement.displayedStartDate?.let { displayedStartDate ->
                                val formattedStartDate = formatDateTimePattern(displayedStartDate)
                                if (isActiveEvent) {
                                    announcement.displayedFinishDate?.let { displayedFinishDate ->
                                        context.getString(
                                            R.string.course_news_on_event_sent,
                                            formattedStartDate,
                                            formatDateTimePattern(displayedFinishDate)
                                        )
                                    }
                                } else {
                                    formattedStartDate
                                }
                            }
                        }
                    }
                }
            return formattedDate ?: formatDateTimePattern(defaultDate)
        }

        private fun formatDateTimePattern(date: Date): String =
            DateTimeHelper.getPrintableDate(date, DateTimeHelper.DISPLAY_DATETIME_COMMA_PATTERN, TimeZone.getDefault())
    }
}