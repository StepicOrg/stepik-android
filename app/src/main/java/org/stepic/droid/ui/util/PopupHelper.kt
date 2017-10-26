package org.stepic.droid.ui.util

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.annotation.LayoutRes
import android.support.v4.widget.PopupWindowCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.PopupWindow
import org.stepic.droid.R


object PopupHelper {

    fun showPopupAnchoredToView(context: Context, anchorView: View, @LayoutRes popupLayout: Int): PopupWindow {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(popupLayout, null)
        val arrowView: View = popupView.findViewById(R.id.arrowView)

        val pos = IntArray(2)
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                popupView.viewTreeObserver.removeGlobalLayoutListener(this)

                anchorView.getLocationOnScreen(pos)
                val offset = pos[0] + anchorView.measuredWidth / 2

                popupView.getLocationOnScreen(pos)

                arrowView.x = offset.toFloat() - pos[0] - arrowView.measuredWidth / 2
            }
        }
        popupView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.animationStyle = R.style.PopupAnimations

        anchorView.post {
            PopupWindowCompat.showAsDropDown(popupWindow, anchorView, 0, 0, Gravity.CENTER)
        }

        return popupWindow
    }
}