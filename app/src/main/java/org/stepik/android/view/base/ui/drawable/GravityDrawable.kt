package org.stepik.android.view.base.ui.drawable

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.annotation.Px

class GravityDrawable(
    private val drawable: Drawable,
    private val gravity: Int,
    @Px
    private val lineHeight: Int
) : Drawable() {
    init {
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    override fun getOpacity(): Int =
        drawable.opacity

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun getIntrinsicHeight(): Int =
        drawable.intrinsicHeight

    override fun getIntrinsicWidth(): Int =
        drawable.intrinsicWidth

    @SuppressLint("CanvasSize")
    override fun draw(canvas: Canvas) {
        canvas.save()
        val dy = canvas.height / 2f - lineHeight / 2
        canvas.translate(0f, if (gravity == Gravity.BOTTOM) dy else -dy)
        drawable.draw(canvas)
        canvas.restore()
    }
}