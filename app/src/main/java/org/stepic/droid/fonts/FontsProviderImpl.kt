package org.stepic.droid.fonts

class FontsProviderImpl : FontsProvider {

    override fun provideFontPath(type: FontType) =
            when (type) {
                FontType.regular -> "fonts/Roboto-Regular.ttf"
                FontType.italic -> "fonts/Roboto-Italic.ttf"
                FontType.bold -> "fonts/Roboto-Bold.ttf"
                FontType.boldItalic -> "fonts/Roboto-BoldItalic.ttf"
            }
}
