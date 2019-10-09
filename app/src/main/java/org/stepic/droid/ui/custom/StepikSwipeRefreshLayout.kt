package org.stepic.droid.ui.custom

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.stepic.droid.R

class StepikSwipeRefreshLayout
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null) : SwipeRefreshLayout(context, attributeSet) {
    init {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.stepik_swipe_refresh))
    }
}
