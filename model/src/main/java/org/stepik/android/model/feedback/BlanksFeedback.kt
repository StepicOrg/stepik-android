package org.stepik.android.model.feedback

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BlanksFeedback(
    @SerializedName("blanks_feedback")
    val blanksFeedback: List<Boolean>? = null
) : Feedback, Serializable