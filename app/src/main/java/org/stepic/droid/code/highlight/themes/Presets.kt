package org.stepic.droid.code.highlight.themes

import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.ColorUtil

object Presets {
    val themes = arrayOf(
        CodeTheme(
                name = App.getAppContext().getString(R.string.light_theme_name),
                syntax = CodeSyntax(
                        plain           = ColorUtil.getColorArgb(R.color.light_theme_plain),
                        string          = ColorUtil.getColorArgb(R.color.light_theme_string),
                        keyword         = ColorUtil.getColorArgb(R.color.light_theme_keyword),
                        comment         = ColorUtil.getColorArgb(R.color.light_theme_comment),
                        type            = ColorUtil.getColorArgb(R.color.light_theme_type),
                        literal         = ColorUtil.getColorArgb(R.color.light_theme_literal),
                        punctuation     = ColorUtil.getColorArgb(R.color.light_theme_punctuation),
                        attributeName   = ColorUtil.getColorArgb(R.color.light_theme_attribute_name),
                        attributeValue  = ColorUtil.getColorArgb(R.color.light_theme_attribute_value)
                ),
                background              = ColorUtil.getColorArgb(R.color.light_theme_background),
                lineNumberBackground    = ColorUtil.getColorArgb(R.color.light_theme_line_number_background),
                lineNumberText          = ColorUtil.getColorArgb(R.color.light_theme_line_number_text),
                selectedLineBackground  = ColorUtil.getColorArgb(R.color.light_theme_selected_line_background),
                lineNumberStroke        = ColorUtil.getColorArgb(R.color.light_theme_line_number_stroke),
                bracketsHighlight       = ColorUtil.getColorArgb(R.color.light_theme_brackets_highlight)
        ),
        CodeTheme(
            name = App.getAppContext().getString(R.string.github_theme_name),
            syntax = CodeSyntax(
                    plain           = ColorUtil.getColorArgb(R.color.github_theme_plain),
                    string          = ColorUtil.getColorArgb(R.color.github_theme_string),
                    comment         = ColorUtil.getColorArgb(R.color.github_theme_comment),
                    type            = ColorUtil.getColorArgb(R.color.github_theme_type),
                    literal         = ColorUtil.getColorArgb(R.color.github_theme_literal),
                    attributeName   = ColorUtil.getColorArgb(R.color.github_theme_attribute_name),
                    attributeValue  = ColorUtil.getColorArgb(R.color.github_theme_attribute_value)
            ),
            background              = ColorUtil.getColorArgb(R.color.github_theme_background),
            lineNumberBackground    = ColorUtil.getColorArgb(R.color.github_theme_line_number_background),
            lineNumberText          = ColorUtil.getColorArgb(R.color.github_theme_line_number_text),
            selectedLineBackground  = ColorUtil.getColorArgb(R.color.github_theme_selected_line_background),
            lineNumberStroke        = ColorUtil.getColorArgb(R.color.github_theme_line_number_stroke)
        ),
        CodeTheme(
            name = App.getAppContext().getString(R.string.tomorrow_night_theme_name),
            syntax = CodeSyntax(
                    plain           = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_plain),
                    string          = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_string),
                    keyword         = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_keyword),
                    comment         = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_comment),
                    type            = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_type),
                    literal         = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_literal),
                    tag             = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_tag),
                    attributeName   = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_attribute_name),
                    attributeValue  = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_attribute_value),
                    declaration     = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_declaration)
            ),
            background              = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_background),
            lineNumberBackground    = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_line_number_background),
            lineNumberText          = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_line_number_text),
            selectedLineBackground  = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_selected_line_background),
            lineNumberStroke        = ColorUtil.getColorArgb(R.color.tomorrow_night_theme_line_number_stroke)
        ),
        CodeTheme(
            name = App.getAppContext().getString(R.string.tranquil_heart_theme_name),
            syntax = CodeSyntax(
                    plain           = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_plain),
                    string          = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_string),
                    keyword         = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_keyword),
                    comment         = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_comment),
                    type            = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_type),
                    literal         = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_literal),
                    tag             = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_tag),
                    attributeName   = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_attribute_name),
                    attributeValue  = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_attribute_value),
                    declaration     = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_declaration)
            ),
            background              = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_background),
            lineNumberBackground    = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_line_number_background),
            lineNumberText          = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_line_number_text),
            selectedLineBackground  = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_selected_line_background),
            lineNumberStroke        = ColorUtil.getColorArgb(R.color.tranquil_heart_theme_line_number_stroke)
        )
    )
}