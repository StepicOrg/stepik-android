package org.stepik.android.presentation.font_size_settings

import org.stepik.android.domain.step_content_text.model.FontSize

interface FontSizeView {
    fun setCachedFontSize(fontSize: FontSize)
}