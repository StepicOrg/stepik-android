package org.stepic.droid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView

class SelectableTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): TextView(context, attrs, defStyleAttr) {
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_DOWN && selectionStart != selectionEnd) {
            val txt = this.text
            this.text = null
            this.text = txt
        }

        return super.dispatchTouchEvent(event)
    }
}