package org.stepik.android.view.step_quiz_choice.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import org.stepic.droid.R
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout

/**
 * Custom item view to infer clicks on the item and scrolling of the webview inside the item
 */
class QuizItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val MAX_CLICK_DURATION = 200
    }

    private var clickDuration: Long = 0

    private lateinit var latexText: LatexSupportableEnhancedFrameLayout
    private val latexTouchEventOffset = resources.getDimension(R.dimen.step_quiz_choice_quiz_item_padding)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val latexTextTouchEvent = MotionEvent.obtain(
            ev.downTime,
            ev.eventTime,
            ev.action,
            ev.x - latexTouchEventOffset,
            ev.y - latexTouchEventOffset,
            ev.metaState
        )

        if (!::latexText.isInitialized) {
            latexText = findViewById(R.id.latex_text)
        }
        latexText.dispatchTouchEvent(latexTextTouchEvent)

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