package org.stepic.droid.viewmodel

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.stepic.droid.R

data class ProfileSettingsViewModel(
        @StringRes val stringRes: Int,
        @ColorRes val textColor: Int = R.color.new_accent_color
)