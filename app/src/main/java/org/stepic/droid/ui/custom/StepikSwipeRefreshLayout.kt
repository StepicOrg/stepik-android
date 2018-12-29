package org.stepic.droid.ui.custom

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import org.stepic.droid.R

class StepikSwipeRefreshLayout
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null) : SwipeRefreshLayout(context, attributeSet) {
    init {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.stepik_swipe_refresh))
    }
}
