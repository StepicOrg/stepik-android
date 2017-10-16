package org.stepic.droid.code.highlight.themes

import android.support.annotation.ColorInt

data class CodeTheme (
        val name: String,
        val syntax: CodeSyntax,
        @ColorInt val background: Int,
        @ColorInt val lineNumberBackground: Int,
        @ColorInt val lineNumberStroke: Int,
        @ColorInt val lineNumberText: Int,
        @ColorInt val selectedLineBackground: Int
)