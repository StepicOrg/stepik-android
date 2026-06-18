package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseBuyAction(
    @SerializedName("enabled")
    val enabled: Boolean
) : Parcelable
