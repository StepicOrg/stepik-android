package org.stepik.android.model.attempts

import com.google.gson.annotations.SerializedName

class Attempt(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("step")
    val step: Long = 0,
    @SerializedName("user")
    val user: Long = 0,

    @SerializedName("dataset")
    private val _dataset: DatasetWrapper? = null,
    @SerializedName("dataset_url")
    val datasetUrl: String? = null,

    @SerializedName("status")
    val status: String? = null,
    @SerializedName("time")
    val time: String? = null,

    @SerializedName("time_left")
    val timeLeft: String? = null
) {
    val dataset: Dataset?
        get() = _dataset?.dataset
}