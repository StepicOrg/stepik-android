package org.stepik.android.view.course_news.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.stepic.droid.R

enum class AnnouncementBadge(
    @DrawableRes
    val backgroundRes: Int,

    @ColorRes
    val textColorRes: Int,

    @DrawableRes
    val compoundDrawableRes: Int,

    @StringRes
    val textRes: Int
) {
    COMPOSING(
        backgroundRes = R.drawable.bg_announcement_composing,
        textColorRes = R.color.color_on_surface_emphasis_medium,
        compoundDrawableRes = R.drawable.ic_announcement_badge_composing,
        textRes = R.string.course_news_composing_badge
    ),
    SCHEDULED(
        backgroundRes = R.drawable.bg_announcement_scheduled,
        textColorRes = R.color.color_overlay_violet,
        compoundDrawableRes = R.drawable.ic_announcement_badge_scheduled,
        textRes = R.string.course_news_scheduled_badge
    ),
    SENDING(
        backgroundRes = R.drawable.bg_announcement_scheduled,
        textColorRes = R.color.color_overlay_violet,
        compoundDrawableRes = R.drawable.ic_announcement_badge_sending,
        textRes = R.string.course_news_sending_badge
    ),
    SENT(
        backgroundRes = R.drawable.bg_announcement_sent,
        textColorRes = R.color.color_overlay_green,
        compoundDrawableRes = R.drawable.ic_announcement_badge_sent,
        textRes = R.string.course_news_sent_badge
    ),
    ON_EVENT(
        backgroundRes = R.drawable.bg_announcement_on_event,
        textColorRes = R.color.color_overlay_blue,
        compoundDrawableRes = -1,
        textRes = R.string.course_news_on_event_badge
    ),
    ONE_TIME(
        backgroundRes = R.drawable.bg_announcement_on_event,
        textColorRes = R.color.color_overlay_blue,
        compoundDrawableRes = -1,
        textRes = R.string.course_news_one_time_badge
    )
}