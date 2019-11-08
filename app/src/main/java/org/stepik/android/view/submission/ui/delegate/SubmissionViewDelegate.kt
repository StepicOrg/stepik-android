package org.stepik.android.view.submission.ui.delegate

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.model.Submission

fun TextView.setSubmission(submission: Submission?, showArrow: Boolean = false) {
    if (submission != null) {
        text = context.getString(R.string.comment_solution_pattern, submission.id)

        @DrawableRes
        val compoundDrawableRes =
            when (submission.status) {
                Submission.Status.CORRECT ->
                    R.drawable.ic_step_quiz_correct

                Submission.Status.WRONG ->
                    R.drawable.ic_step_quiz_wrong_wide

                else ->
                    -1
            }
        setCompoundDrawables(start = compoundDrawableRes, end = if (showArrow) R.drawable.ic_nav_arrow_right else -1)
        isVisible = true
    } else {
        isVisible = false
    }
}