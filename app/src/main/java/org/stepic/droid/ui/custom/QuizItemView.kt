package org.stepic.droid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebView
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import timber.log.Timber
import java.util.*

/**
 * Custom item view to infer clicks on the item and scrolling of the webview inside the item
 */
class QuizItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val MAX_CLICK_DURATION = 200
    }

    private var startClickTime: Long = 0

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val webview = getChildAt(2) as ProgressLatexView
        val dispatched = webview.webView.dispatchTouchEvent(ev)
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            startClickTime = Calendar.getInstance().timeInMillis
        }
        if (ev?.action == MotionEvent.ACTION_UP) {
            onTouchEvent(ev)
        }
        super.onTouchEvent(ev)
        return dispatched
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun performClick(): Boolean {
        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
        if (clickDuration < MAX_CLICK_DURATION) {
            return super.performClick()
        }
        return false
    }
}