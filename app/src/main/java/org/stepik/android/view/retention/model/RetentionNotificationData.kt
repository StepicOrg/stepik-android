package org.stepik.android.view.retention.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RetentionNotificationData(val retentionDay: Int) : Parcelable
