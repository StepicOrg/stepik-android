package org.stepic.droid.ui.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
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
    private fun calcArrowHorizontalOffset(anchorView: View, popupView: View, arrowView: View): Float {
        val pos = IntArray(2)
        anchorView.getLocationOnScreen(pos)
        val anchorOffset = pos[0] + anchorView.measuredWidth / 2

        popupView.getLocationOnScreen(pos)
        return anchorOffset.toFloat() - pos[0] - arrowView.measuredWidth / 2
    }


    fun showPopupAnchoredToView(context: Context, anchorView: View?, popupText: String, cancelableOnTouchOutside: Boolean = false): PopupWindow? {
        anchorView ?: return null

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window, null)

        popupView.popupText.text = popupText

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                popupView.viewTreeObserver.removeGlobalLayoutListener(this)
                popupView.arrowView.x = calcArrowHorizontalOffset(anchorView, popupView, popupView.arrowView)
            }
        }
        popupView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.animationStyle = R.style.PopupAnimations
        popupWindow.isOutsideTouchable = cancelableOnTouchOutside

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }

        anchorView.post {
            if (anchorView.windowToken != null) {
                PopupWindowCompat.showAsDropDown(popupWindow, anchorView, 0, 0, Gravity.CENTER)
            }
        }

        return popupWindow
    }
}