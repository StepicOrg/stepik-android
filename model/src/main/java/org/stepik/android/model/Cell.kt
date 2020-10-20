package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cell(
    @SerializedName("name")
    val name: String,
    @SerializedName("answer")
    var answer: Boolean
) : Parcelable