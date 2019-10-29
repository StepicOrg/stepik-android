package org.stepik.android.model.feedback

import java.io.Serializable

data class StringFeedback(
    val stringFeedback: String? = null
) : Feedback, Serializable