package org.stepik.android.view.font_size_settings.model

data class FontItem(
    val title: String,
    val fontSize: Size
) {
    enum class Size(val size: Float) {
        SMALL(14f),
        MEDIUM(16f),
        LARGE(18f)
    }
}