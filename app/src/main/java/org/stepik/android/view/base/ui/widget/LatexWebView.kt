package org.stepik.android.view.base.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.stepic.droid.BuildConfig
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.toDp
import org.stepik.android.view.base.ui.widget.attributes.TextAttributes
import kotlin.math.abs

@SuppressLint("AddJavascriptInterface")
class LatexWebView
@JvmOverloads
constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr),
    View.OnLongClickListener,
    View.OnClickListener,
    View.OnTouchListener {

    companion object {
        private const val MAX_CLICK_DURATION = 200

        private const val JS_NAME = HtmlHelper.HORIZONTAL_SCROLL_LISTENER
    }

    var attributes = TextAttributes.fromAttributeSet(context, attrs)
        set(value) {
            field = value
            settings.defaultFontSize = value.textSize

            setOnLongClickListener(this.takeIf { value.textIsSelectable })
        }

    var onImageClickListener: OnImageClickListener? = null

    private var scrollState = ScrollState()

    private var startX = 0f
    private var startY = 0f

    init {
        setBackgroundColor(Color.argb(1, 0, 0, 0))
        setOnLongClickListener(this.takeIf { attributes.textIsSelectable })

        isFocusable = true
        isFocusableInTouchMode = true

        settings.domStorageEnabled = true
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.defaultFontSize = attributes.textSize
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
        }

        addJavascriptInterface(OnScrollWebListener(), JS_NAME)
        isSoundEffectsEnabled = false
    }

    override fun onLongClick(v: View?): Boolean = true

    override fun onClick(v: View?) {
        val hr = hitTestResult
        try {
            if (hr.type == HitTestResult.IMAGE_TYPE) {
                onImageClickListener?.onImageClick(hr.extra ?: return)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP &&
            event.downTime - event.eventTime < MAX_CLICK_DURATION
        ) {
            performLongClick()
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y

                val dpx = event.x.toDp()
                val dpy = event.y.toDp()
                evalScript("measureScroll($dpx, $dpy)")
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = startX - event.x
                val dy = startY - event.y
                event.setLocation(event.x, event.y)

                if (abs(dx) > abs(dy) && canScrollHorizontally(dx.toInt())) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                scrollState.reset()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun canScrollHorizontally(dx: Int): Boolean =
        super.canScrollHorizontally(dx) ||
        dx < 0 && scrollState.canScrollLeft ||
        dx > 0 && scrollState.canScrollRight
    
    private fun evalScript(code: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(code, null)
        } else {
            loadUrl("javascript: $code")
        }
    }

    interface OnImageClickListener {
        fun onImageClick(imagePath: String)
    }

    private inner class OnScrollWebListener {
        @JavascriptInterface
        fun onScroll(offsetWidth: Float, scrollWidth: Float, scrollLeft: Float) {
            scrollState.canScrollLeft = scrollLeft > 0
            scrollState.canScrollRight = offsetWidth + scrollLeft < scrollWidth
        }
    }

    private class ScrollState(
        internal var canScrollLeft: Boolean = false,
        internal var canScrollRight: Boolean = false
    ) {
        internal fun reset() {
            canScrollLeft = false
            canScrollRight = false
        }
    }
}