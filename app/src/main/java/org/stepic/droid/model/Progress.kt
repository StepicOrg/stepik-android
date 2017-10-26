package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

class Progress {
    var id: String? = null
    @SerializedName("last_viewed")
    var lastViewed: String? = null
    var score: String? = null
    var cost: Int = 0
    @SerializedName("n_steps")
    var nSteps: Int = 0
    @SerializedName("n_steps_passed")
    var nStepsPassed: Int = 0
    @SerializedName("is_passed")
    var isPassed = false
}
