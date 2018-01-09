package org.stepic.droid.adaptive.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView


class CardScrollView
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attributeSet, defStyleAttr) {

    fun canScrollVertically() = canScrollVertically(-1) || canScrollVertically(1)

    override fun onTouchEvent(ev: MotionEvent?) =
            if (ev?.action == MotionEvent.ACTION_DOWN)
                canScrollVertically()
            else
                super.onTouchEvent(ev)
}