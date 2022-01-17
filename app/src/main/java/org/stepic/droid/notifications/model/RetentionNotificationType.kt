package org.stepic.droid.notifications.model

import androidx.annotation.StringRes
import org.stepic.droid.R

enum class RetentionNotificationType(
    @StringRes
    val titleRes: Int,
    @StringRes
    val messageRes: Int
) {
    DAY1(R.string.retention_notification_day1_title, R.string.retention_notification_day1_message),
    DAY3(R.string.retention_notification_day3_title, R.string.retention_notification_day3_message);

    fun getDayInt(): Int =
        when (this.name) {
            DAY1.name ->
                1
            DAY3.name ->
                3
            else ->
                throw IllegalStateException("Invalid enum state")
        }
}