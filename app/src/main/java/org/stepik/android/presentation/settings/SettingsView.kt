package org.stepik.android.presentation.settings

interface SettingsView {
    fun setBlockingLoading(isLoading: Boolean)
    fun onLogoutSuccess()
}