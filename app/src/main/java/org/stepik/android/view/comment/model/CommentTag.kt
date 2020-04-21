package org.stepik.android.view.comment.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.stepic.droid.R

enum class CommentTag(
    @DrawableRes
    val backgroundRes: Int,

    @ColorRes
    val textColorRes: Int,

    @DrawableRes
    val compoundDrawableRes: Int,

    @StringRes
    val textRes: Int
) {
    COURSE_TEAM(
        backgroundRes = R.drawable.bg_comment_tag_course_team,
        textColorRes = R.color.white,
        compoundDrawableRes = -1,
        textRes = R.string.comment_tag_course_team
    ),
    STAFF(
        backgroundRes = R.drawable.bg_comment_tag_course_team,
        textColorRes = R.color.white,
        compoundDrawableRes = -1,
        textRes = R.string.comment_tag_staff
    ),
    PINNED(
        backgroundRes = R.drawable.bg_comment_tag_pinned,
        textColorRes = R.color.white,
        compoundDrawableRes = R.drawable.ic_comment_tag_pinned,
        textRes = R.string.comment_tag_pinned
    ),
    MODERATOR(
        backgroundRes = R.drawable.bg_comment_tag_course_moderator,
        textColorRes = R.color.white,
        compoundDrawableRes = -1,
        textRes = R.string.comment_tag_moderator
    )
}