package org.stepic.droid.ui.custom

import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.MainThread
import androidx.appcompat.widget.AppCompatImageView
import org.stepic.droid.R

class ArrowImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : AppCompatImageView(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    companion object {
        private const val IS_ARROW_BOTTOM_DEFAULT = true
        private const val parentParcelableStateKey = "parentParcelableStateKey"
        private const val isArrowBottomKey = "isArrowBottomKey"
    }

    private val arrowBottomToTopRes = R.drawable.avd_arrow_bottom_to_top
    private val arrowTopToBottomRes = R.drawable.avd_arrow_top_to_bottom
    private var isArrowBottom: Boolean = IS_ARROW_BOTTOM_DEFAULT

    init {
        setImageResourceByState(IS_ARROW_BOTTOM_DEFAULT)
    }

    private fun setImageResourceByState(isArrowBottomInternal: Boolean) {
        val defaultDrawableRes = if (isArrowBottomInternal) {
            arrowBottomToTopRes
        } else {
            arrowTopToBottomRes
        }
        setImageResource(defaultDrawableRes)
    }

    /**
     * change expand/collapse state
     */
    @MainThread
    fun changeState() {
        setImageResourceByState(isArrowBottom)
        (drawable as Animatable).start()
        isArrowBottom = !isArrowBottom
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(parentParcelableStateKey, superState)
        bundle.putBoolean(isArrowBottomKey, isArrowBottom)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && state is Bundle) {
            val parentParcelable = state.getParcelable<Parcelable>(parentParcelableStateKey)
            super.onRestoreInstanceState(parentParcelable)
            isArrowBottom = state.getBoolean(isArrowBottomKey)
            setImageResourceByState(isArrowBottom)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun isExpanded(): Boolean =
        !isArrowBottom
}