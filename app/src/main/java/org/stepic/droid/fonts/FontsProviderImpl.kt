package org.stepic.droid.fonts

class FontsProviderImpl : FontsProvider {
    override fun provideFontName(type: FontType) =
            when (type) {
                FontType.regular -> "Roboto-Regular"
                FontType.italic -> "Roboto-Italic"
                FontType.bold -> "Roboto-Bold"
                FontType.boldItalic -> "Roboto-BoldItalic"
            }
}
