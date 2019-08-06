package org.stepik.android.view.step_quiz_choice.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.custom.ProgressLatexView
import org.stepic.droid.util.DpPixelsHelper

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
        const val LATEX_TOUCH_EVENT_OFFSET = 16f
    }

    private var clickDuration: Long = 0

    private lateinit var webView: ProgressLatexView
    private lateinit var latexText: LatexSupportableEnhancedFrameLayout

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!::webView.isInitialized) {
            webView = findViewById(R.id.itemChoiceLatex)
        }
        val dispatched = webView.webView.dispatchTouchEvent(ev)

        val latexTextTouchEvent = MotionEvent.obtain(
            ev.downTime,
            ev.eventTime,
            ev.action,
            ev.x - DpPixelsHelper.convertDpToPixel(LATEX_TOUCH_EVENT_OFFSET),
            ev.y - DpPixelsHelper.convertDpToPixel(LATEX_TOUCH_EVENT_OFFSET),
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
        return dispatched
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = false

    override fun performClick(): Boolean {
        if (clickDuration < MAX_CLICK_DURATION) {
            return super.performClick()
        }
        return false
    }
}