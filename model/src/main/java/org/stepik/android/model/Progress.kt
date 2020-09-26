package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class Progress(
    @SerializedName("id")
    override val id: String,
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
) : Parcelable, Identifiable<String>
