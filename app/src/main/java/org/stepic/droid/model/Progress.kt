package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

data class Progress(
        val id: String? = null,
        @SerializedName("last_viewed")
        val lastViewed: String? = null,
        var score: String? = null,
        val cost: Int = 0,
        @SerializedName("n_steps")
        val nSteps: Int = 0,
        @SerializedName("n_steps_passed")
        val nStepsPassed: Int = 0,
        @SerializedName("is_passed")
        val isPassed: Boolean = false
)
