package org.stepic.droid.adaptive.ui.custom.morphing

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.TextView
import org.stepic.droid.util.KotlinUtil.setIfNot

class MorphingView
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attributeSet, defStyleAttr) {

    val drawableWrapper = GradientDrawableWrapper(GradientDrawable())

    var nestedTextView: TextView? = null


    init {
        background = drawableWrapper.drawable
    }

    fun setGradientDrawableParams(color: Int, cornerRadius: Float) {
        drawableWrapper.drawable.shape = GradientDrawable.RECTANGLE
        drawableWrapper.color = color
        drawableWrapper.cornerRadius = cornerRadius
    }

    fun morph(params: MorphParams) {

        setIfNot(drawableWrapper::cornerRadius::set, params.cornerRadius, -1f)
        setIfNot(drawableWrapper::color::set, params.backgroundColor, -1)

        val lParams = this.layoutParams as MarginLayoutParams

        setIfNot(lParams::width::set, params.width, -1)
        setIfNot(lParams::height::set, params.height, -1)

        setIfNot(lParams::leftMargin::set, params.marginLeft, -1)
        setIfNot(lParams::topMargin::set, params.marginTop, -1)
        setIfNot(lParams::rightMargin::set, params.marginRight, -1)
        setIfNot(lParams::bottomMargin::set, params.marginBottom, -1)

        layoutParams = lParams

        nestedTextView?.let {
            if (params.text != null) it.text = params.text

            setIfNot({ it@nestedTextView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, it) }, params.textSize, -1f)
        }
    }

    fun getMorphParams(): MorphParams {
        val lParams = layoutParams as MarginLayoutParams

        return MorphParams(
                drawableWrapper.cornerRadius,
                drawableWrapper.color,

                width,
                height,

                lParams.leftMargin,
                lParams.topMargin,
                lParams.rightMargin,
                lParams.bottomMargin,

                nestedTextView?.text?.toString() ?: "",
                nestedTextView?.textSize ?: -1f
        )
    }

    val initialMorphParams by lazy { getMorphParams() }

    data class MorphParams(
            val cornerRadius: Float = -1f,

            val backgroundColor: Int = -1,

            val width: Int = -1,
            val height: Int = -1,

            val marginLeft: Int = -1,
            val marginTop: Int = -1,
            val marginRight: Int = -1,
            val marginBottom: Int = -1,

            val text: String? = null,
            val textSize: Float = -1f
    )
}