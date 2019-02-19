package org.stepic.droid.notifications.model

import android.support.annotation.StringRes

enum class RetentionNotificationType(
    @StringRes
    val titleRes: Int,
    @StringRes
    val messageRes: Int
) {
    DAY1(0, 0),
    DAY3(0, 0)
}