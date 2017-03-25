package org.stepic.droid.fonts

interface FontsProvider {
    fun provideFontName(type: FontType) : String
}
