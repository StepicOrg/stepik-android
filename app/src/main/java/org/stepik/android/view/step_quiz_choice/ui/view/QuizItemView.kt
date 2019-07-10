package org.stepik.android.view.step_quiz_choice.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.ProgressLatexView

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

    private var startClickTime: Long = 0
    private var clickDuration: Long = 0

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val webview = findViewById<ProgressLatexView>(R.id.itemChoiceLatex)
        val dispatched = webview.webView.dispatchTouchEvent(ev)
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            startClickTime = ev.eventTime
        }
        if (ev?.action == MotionEvent.ACTION_UP) {
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