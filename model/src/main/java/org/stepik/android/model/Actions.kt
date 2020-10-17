package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Actions(
    @SerializedName("vote")
    val vote: Boolean = false,
    @SerializedName("edit")
    val edit: Boolean = false,
    @SerializedName("delete")
    val delete: Boolean = false,
    @SerializedName("pin")
    val pin: Boolean = false,

    @SerializedName("test_section")
    val testSection: String? = null,
    @SerializedName("do_review")
    val doReview: String? = null,
    @SerializedName("edit_instructions")
    val editInstructions: String? = null
) : Parcelable