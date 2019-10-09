package org.stepik.android.view.lesson.ui.delegate

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.PopupWindowCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.tooltip_lesson_info.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.doOnPreDraw

class LessonInfoTooltipDelegate(
    private val view: View
) {
    fun showLessonInfoTooltip(
        stepScore: Long,
        stepCost: Long,
        lessonTimeToCompleteInSeconds: Long,
        certificateThreshold: Long
    ) {
        val anchorView = view
            .findViewById<View>(R.id.lesson_menu_item_info)
            ?: return

        val popupView = LayoutInflater
            .from(anchorView.context)
            .inflate(R.layout.tooltip_lesson_info, null)

        if (stepScore > 0) {
            popupView
                .stepWorth
                .setItem(stepScore, stepCost, R.string.lesson_info_points_with_score, R.plurals.points, R.drawable.ic_check_rounded)
        } else {
            popupView
                .stepWorth
                .setItem(stepCost, R.string.lesson_info_points, R.plurals.points, R.drawable.ic_check_rounded)
        }

        val (timeValue, @PluralsRes timeUnitPlural) =
            if (lessonTimeToCompleteInSeconds in 0 until 3600) {
                lessonTimeToCompleteInSeconds / 60 to R.plurals.minutes
            } else {
                lessonTimeToCompleteInSeconds / 3600 to R.plurals.hours
            }

        popupView
            .lessonTimeToComplete
            .setItem(timeValue, R.string.lesson_info_time_to_complete, timeUnitPlural, R.drawable.ic_duration)

        popupView
            .certificateThreshold
            .setItem(certificateThreshold, R.string.lesson_info_certificate_threshold, R.plurals.points, R.drawable.ic_lesson_info)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.animationStyle = R.style.PopupAnimations
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable()) // workaround to make isOutsideTouchable work on Android 4

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }

        popupView.doOnPreDraw {
            popupView.arrowView?.x = calcArrowHorizontalOffset(anchorView, popupView, popupView.arrowView)
        }

        anchorView.post {
            if (anchorView.windowToken != null) {
                PopupWindowCompat.showAsDropDown(popupWindow, anchorView, 0, 0, Gravity.CENTER)
            }
        }
    }

    private fun AppCompatTextView.setItem(
        value: Long,
        @StringRes stringRes: Int,
        @PluralsRes pluralRes: Int,
        @DrawableRes drawableRes: Int
    ) {
        if (value > 0) {
            setItemDrawable(drawableRes)
            text = context.getString(stringRes, resources.getQuantityString(pluralRes, value.toInt(), value))
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }

    // 'StepProgress.score' points out of 'StepProgress.cost' (for step)
    private fun AppCompatTextView.setItem(
        stepScore: Long,
        stepCost: Long,
        @StringRes stringRes: Int,
        @PluralsRes pluralRes: Int,
        @DrawableRes drawableRes: Int
    ) {
        setItemDrawable(drawableRes)
        text = context.getString(stringRes, resources.getQuantityString(pluralRes, stepScore.toInt(), stepScore), stepCost)
        visibility = View.VISIBLE
    }

    private fun AppCompatTextView.setItemDrawable(@DrawableRes drawableRes: Int) {
        val iconDrawable = AppCompatResources
                .getDrawable(context, drawableRes)
                ?.let(DrawableCompat::wrap)
                ?.let(Drawable::mutate)
            ?: return
        DrawableCompat.setTint(iconDrawable, ContextCompat.getColor(context,  android.R.color.white))
        setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)
    }

    private fun calcArrowHorizontalOffset(anchorView: View, popupView: View, arrowView: View): Float {
        val pos = IntArray(2)
        anchorView.getLocationOnScreen(pos)
        val anchorOffset = pos[0] + anchorView.measuredWidth / 2

        popupView.getLocationOnScreen(pos)
        return anchorOffset.toFloat() - pos[0] - arrowView.measuredWidth / 2
    }
}