package org.stepik.android.presentation.step_quiz_choice.model

import android.support.annotation.ColorRes
import org.stepic.droid.R

enum class ChoiceColor(
    @ColorRes
    val backgroundColor: Int,
    @ColorRes
    val strokeColor: Int
) {
    CHECKED(R.color.choice_checked, R.color.choice_checked_border),
    NOT_CHECKED(R.color.choice_not_checked, R.color.choice_not_checked_border),
    CORRECT(R.color.choice_correct, R.color.choice_correct_border),
    INCORRECT(R.color.choice_incorrect, R.color.choice_incorrect_border)
}