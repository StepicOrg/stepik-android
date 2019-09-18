package org.stepik.android.presentation.font_size_settings

interface FontSizeView {
    fun onFontSizeChosen()
    fun setCachedFontSize(fontSize: Float)
}