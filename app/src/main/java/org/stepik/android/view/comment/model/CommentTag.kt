package org.stepik.android.view.comment.model

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
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
    STAFF_REPLIED(
        backgroundRes = R.drawable.bg_comment_tag_staff_replied,
        textColorRes = R.color.white,
        compoundDrawableRes = -1,
        textRes = R.string.comment_tag_staff_replied
    ),
    PINNED(
        backgroundRes = R.drawable.bg_comment_tag_pinned,
        textColorRes = R.color.white,
        compoundDrawableRes = R.drawable.ic_comment_tag_pinned,
        textRes = R.string.comment_tag_pinned
    )
}