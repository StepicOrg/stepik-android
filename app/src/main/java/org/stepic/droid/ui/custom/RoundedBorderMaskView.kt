package org.stepic.droid.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout

class RoundedBorderMaskView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var borderRadius: Float = 0f

    private var maskBitmap: Bitmap? = null
    private var paint: Paint? = null
    private var maskPaint: Paint? = null

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        maskPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        maskPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        val offscreenBitmap = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
        val offscreenCanvas = Canvas(offscreenBitmap)

        super.draw(offscreenCanvas)

        if (maskBitmap == null) {
            maskBitmap = createMask(canvas.width, canvas.height)
        }

        offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)
        canvas.drawBitmap(offscreenBitmap, 0f, 0f, paint)
    }

    private fun createMask(width: Int, height: Int): Bitmap {
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(mask)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), borderRadius, borderRadius, paint)

        return mask
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        maskBitmap = createMask(w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }
}