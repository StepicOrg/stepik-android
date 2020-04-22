package org.stepik.android.view.submission.ui.delegate

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import org.stepic.droid.R
import org.stepic.droid.util.resolveResourceIdAttribute
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

        val startDrawable =
            if (compoundDrawableRes != -1) {
                AppCompatResources.getDrawable(context, compoundDrawableRes)
            } else {
                null
            }

        val endDrawable =
            if (showArrow) {
                AppCompatResources
                    .getDrawable(context, R.drawable.ic_nav_arrow_right)
                    ?.apply {
                        mutate()
                        DrawableCompat.setTintList(this, AppCompatResources.getColorStateList(context, context.resolveResourceIdAttribute(R.attr.colorControlNormal)))
                    }
            } else {
                null
            }

        TextViewCompat
            .setCompoundDrawablesRelativeWithIntrinsicBounds(this, startDrawable, null, endDrawable, null)
        isVisible = true
    } else {
        isVisible = false
    }
}