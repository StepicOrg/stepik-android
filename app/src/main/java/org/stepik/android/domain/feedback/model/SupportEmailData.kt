package org.stepik.android.domain.feedback.model

data class SupportEmailData(
    val mailTo: String,
    val subject: String,
    val body: String
)