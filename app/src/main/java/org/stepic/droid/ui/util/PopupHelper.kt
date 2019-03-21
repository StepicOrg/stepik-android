package org.stepic.droid.ui.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.support.annotation.DrawableRes
import android.support.v4.widget.PopupWindowCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.PopupWindow
import org.stepic.droid.R

import kotlinx.android.synthetic.main.popup_window.view.*


object PopupHelper {
    enum class PopupTheme(
        @DrawableRes
        val arrowRes: Int,
        @DrawableRes
        val backgroundRes: Int
    ) {
        DARK(R.drawable.popup_arrow_up, R.drawable.background_popup),
        LIGHT(R.drawable.popup_arrow_up_light, R.drawable.background_popup_light),
        DARK_ABOVE(R.drawable.popup_arrow_down, R.drawable.background_popup),
        LIGHT_ABOVE(R.drawable.popup_arrow_down_light, R.drawable.background_popup_light)
    }

    private fun calcArrowHorizontalOffset(anchorView: View, popupView: View, arrowView: View): Float {
        val pos = IntArray(2)
        anchorView.getLocationOnScreen(pos)
        val anchorOffset = pos[0] + anchorView.measuredWidth / 2

        popupView.getLocationOnScreen(pos)
        return anchorOffset.toFloat() - pos[0] - arrowView.measuredWidth / 2
    }


    fun showPopupAnchoredToView(
        context: Context, anchorView: View?,
        popupText: String, theme: PopupTheme = PopupTheme.DARK,
        cancelableOnTouchOutside: Boolean = false,
        gravity: Int = Gravity.CENTER,
        withArrow: Boolean = false,
        isAboveAnchor: Boolean = false
    ): PopupWindow? {
        anchorView ?: return null

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflatePopupWindow(inflater, withArrow, isAboveAnchor)

        val popupTextView = popupView.popupText
        val popupArrowView = popupView.arrowView

        popupTextView.text = popupText
        popupTextView.setBackgroundResource(theme.backgroundRes)

        popupArrowView.setBackgroundResource(theme.arrowRes)
        popupArrowView.changeVisibility(withArrow)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.animationStyle = R.style.PopupAnimations
        popupWindow.isOutsideTouchable = cancelableOnTouchOutside

        if (withArrow) {
            popupView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                var offsetY = 0
                override fun onGlobalLayout() {
                    popupArrowView.x = calcArrowHorizontalOffset(anchorView, popupView, popupView.arrowView)
                    if (isAboveAnchor) {
                        if (offsetY == -(anchorView.measuredHeight + popupView.measuredHeight)) {
                            popupView.viewTreeObserver.removeGlobalLayoutListener(this)
                            return
                        } else {
                            offsetY = -(anchorView.measuredHeight + popupView.measuredHeight)
                        }
                        popupWindow.update(
                            anchorView,
                            0,
                            offsetY,
                            popupWindow.width,
                            popupWindow.height
                        )
                    }
                }
            })
        }

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }

        anchorView.post {
            if (anchorView.windowToken != null) {
                if (withArrow) {
                    PopupWindowCompat.showAsDropDown(popupWindow, anchorView, 0, 0, gravity)
                } else {
                    popupWindow.showAtLocation(anchorView, gravity, 0, 0)
                }
            }
        }

        return popupWindow
    }

    private fun inflatePopupWindow(inflater: LayoutInflater, withArrow: Boolean, isAboveAnchor: Boolean): View =
        if (withArrow && isAboveAnchor) {
            inflater.inflate(R.layout.popup_window_down, null)
        } else {
            inflater.inflate(R.layout.popup_window, null)
        }
}