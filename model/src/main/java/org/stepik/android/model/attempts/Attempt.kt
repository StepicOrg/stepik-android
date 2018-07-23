package org.stepik.android.model.attempts

import com.google.gson.annotations.SerializedName

class Attempt(
        val id: Long = 0,
        val step: Long = 0,
        val user: Long = 0,

        @SerializedName("dataset")
        private val _dataset: DatasetWrapper? = null,
        @SerializedName("dataset_url")
        val datasetUrl: String? = null,

        val status: String? = null,
        val time: String? = null,

        @SerializedName("time_left")
        val timeLeft: String? = null
) {
    val dataset: Dataset?
        get() = _dataset?.dataset
}