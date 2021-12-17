package org.stepik.android.presentation.settings

import org.stepik.android.domain.feedback.model.SupportEmailData

interface SettingsView {
    fun setBlockingLoading(isLoading: Boolean)
    fun sendTextFeedback(supportEmailData: SupportEmailData)
    fun onLogoutSuccess()
}