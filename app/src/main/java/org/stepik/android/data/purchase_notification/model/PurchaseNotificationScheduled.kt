package org.stepik.android.data.purchase_notification.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseNotificationScheduled(
    val courseId: Long,
    val scheduledTime: Long
) : Parcelable