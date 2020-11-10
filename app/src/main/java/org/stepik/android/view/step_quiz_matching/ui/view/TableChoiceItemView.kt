package org.stepik.android.view.step_quiz_matching.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.marginLeft
import org.stepik.android.view.latex.ui.widget.LatexView

class TableChoiceItemView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        const val MAX_CLICK_DURATION = 200
    }

    private var clickDuration: Long = 0

    private lateinit var compoundButton: View
    private lateinit var latexText: View

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!::compoundButton.isInitialized) {
            compoundButton = children.first { it is CompoundButton }
        }
        val xOffset = -(compoundButton.width + compoundButton.marginLeft).toFloat()
        ev.offsetLocation(xOffset, 0f)

        if (!::latexText.isInitialized) {
            latexText = children.first { it is LatexView }
        }
        latexText.dispatchTouchEvent(ev)

        if (ev.action == MotionEvent.ACTION_UP) {
            clickDuration = ev.eventTime - ev.downTime
        }
        super.onTouchEvent(ev)
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = false

    override fun performClick(): Boolean {
        if (clickDuration < MAX_CLICK_DURATION) {
            return super.performClick()
        }
        return false
    }
}