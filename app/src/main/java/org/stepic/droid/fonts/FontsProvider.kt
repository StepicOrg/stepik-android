package org.stepic.droid.fonts

interface FontsProvider {
    fun provideFontPath(type: FontType): String
}
