package org.stepik.android.domain.feedback.model

import java.io.File

data class SupportEmailData(
    val mailTo: String,
    val subject: String,
    val body: File
)