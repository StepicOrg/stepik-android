package org.stepic.droid.fonts

import javax.inject.Inject

class FontsProviderImpl @Inject constructor() : FontsProvider {

    override fun provideFontPath(type: FontType) =
            when (type) {
                FontType.regular -> "fonts/Roboto-Regular.ttf"
                FontType.italic -> "fonts/Roboto-Italic.ttf"
                FontType.bold -> "fonts/Roboto-Bold.ttf"
                FontType.boldItalic -> "fonts/Roboto-BoldItalic.ttf"
                FontType.medium -> "fonts/Roboto-Medium.ttf"
                FontType.light -> "fonts/Roboto-Light.ttf"
                FontType.mono -> "fonts/PT-Mono.ttf"
            }
}
