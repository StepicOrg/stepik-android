package org.stepik.android.view.comment.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.stepic.droid.R

enum class DiscussionThreadContainer(
    @StringRes
    val disabledStringRes: Int,

    @StringRes
    val showStringRes: Int,

    @StringRes
    val writeFirstStringRes: Int,

    @DrawableRes
    val containerDrawable: Int
) {
    DEFAULT(
        disabledStringRes = R.string.comment_disabled,
        showStringRes = R.string.step_discussion_show,
        writeFirstStringRes = R.string.step_discussion_write_first,
        containerDrawable = R.drawable.ic_step_discussion
    ),
    SOLUTIONS(
        disabledStringRes = R.string.solution_disabled,
        showStringRes = R.string.step_solutions_show,
        writeFirstStringRes = R.string.step_solutions_write_first,
        containerDrawable = R.drawable.ic_step_solutions
    )
}