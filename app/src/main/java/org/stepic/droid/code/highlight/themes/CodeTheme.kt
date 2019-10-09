package org.stepic.droid.code.highlight.themes

import androidx.annotation.ColorInt
import org.stepic.droid.R
import org.stepic.droid.util.ColorUtil

class CodeTheme (
        val name: String,
        val syntax: CodeSyntax,
        @ColorInt val background: Int,
        @ColorInt val lineNumberBackground: Int,
        @ColorInt val lineNumberStroke: Int,
        @ColorInt val lineNumberText: Int,
        @ColorInt val selectedLineBackground: Int,
        @ColorInt val bracketsHighlight: Int = ColorUtil.getColorArgb(R.color.default_code_brackets_highlight_color),
        @ColorInt val errorHighlight: Int = ColorUtil.getColorArgb(R.color.default_code_error_highlight_color)
)