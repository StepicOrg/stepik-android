package org.stepik.android.model.feedback

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChoiceFeedback(
    @SerializedName("options_feedback")
    val optionsFeedback: List<String>? = null
) : Feedback, Serializable