package org.stepik.android.view.step_quiz_code.model

import androidx.annotation.ColorRes
import org.stepic.droid.R

enum class CodeOutputColors(
    @ColorRes
    val titleColor: Int,

    @ColorRes
    val bodyColor: Int,

    @ColorRes
    val backgroundColor: Int
) {
    STANDARD(
        titleColor = R.color.color_body1,
        bodyColor = R.color.new_accent_color,
        backgroundColor = R.color.run_code_output_background
    ),
    ERROR(
        titleColor = R.color.step_quiz_code_output_title,
        bodyColor = R.color.red02,
        backgroundColor = R.color.red03
    )
}