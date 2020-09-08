package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Progress(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("last_viewed")
    val lastViewed: String? = null, //in SECONDS
    @SerializedName("score")
    var score: String? = null,
    @SerializedName("cost")
    val cost: Long = 0,
    @SerializedName("n_steps")
    val nSteps: Long = 0,
    @SerializedName("n_steps_passed")
    val nStepsPassed: Long = 0,
    @SerializedName("is_passed")
    val isPassed: Boolean = false
) : Parcelable
