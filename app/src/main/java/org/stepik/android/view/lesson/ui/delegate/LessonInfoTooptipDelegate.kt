package org.stepik.android.view.lesson.ui.delegate

import android.support.annotation.DrawableRes
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.tooltip_lesson_info.view.*
import org.stepic.droid.R

class LessonInfoTooptipDelegate(
    private val view: View
) {
    fun showLessonInfoTooltip(
        stepWorth: Long,
        lessonTimeToCompleteInSeconds: Long,
        certificateThreshold: Long
    ) {
        val anchorView = view
            .findViewById<View>(R.id.lesson_menu_item_info)
            ?: return

        val context = view.context
        val resources = context.resources

        val popupView = LayoutInflater
            .from(anchorView.context)
            .inflate(R.layout.tooltip_lesson_info, null)

        popupView
            .stepWorth
            .setItem(stepWorth, R.string.lesson_info_points, R.plurals.points, R.drawable.ic_check_rounded)


        popupView
            .lessonTimeToComplete
            .setItem(lessonTimeToCompleteInSeconds, R.string.lesson_info_time_to_complete, R.plurals.minutes, R.drawable.ic_duration)
    }

    private fun AppCompatTextView.setItem(
        value: Long,
        @StringRes stringRes: Int,
        @PluralsRes pluralRes: Int,
        @DrawableRes drawableRes: Int
    ) {
        if (value > 0) {
            val iconDrawable = AppCompatResources
                .getDrawable(context, drawableRes)
                ?.let(DrawableCompat::wrap)
                ?: return
            DrawableCompat.setTint(iconDrawable, ContextCompat.getColor(context,  android.R.color.white))
            setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)

            text = context.getString(stringRes, resources.getQuantityString(pluralRes, value.toInt(), stepWorth))
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }
}