package org.stepik.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CanBeBought(
    val enabled: Boolean
) : Parcelable
