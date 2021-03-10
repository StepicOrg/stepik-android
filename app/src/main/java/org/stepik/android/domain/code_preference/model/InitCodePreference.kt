package org.stepik.android.domain.code_preference.model

data class InitCodePreference(
    val sourceStepId: Long,
    val language: String,
    val codeTemplates: Map<String, String>
)
