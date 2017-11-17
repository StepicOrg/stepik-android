package org.stepic.droid.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import org.stepic.droid.R

class RoundedBorderMaskView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var borderRadius: Float = 0f
        set(value) {
            field = value
            updateMask(width, height)
        }

    private var maskBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedBorderMaskView)
        try {
            borderRadius = attributes.getDimension(R.styleable.RoundedBorderMaskView_borderRadius, 0f)
        } finally {
            attributes.recycle()
        }

        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        val offscreenBitmap = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
        val offscreenCanvas = Canvas(offscreenBitmap)

        super.draw(offscreenCanvas)

        if (maskBitmap == null) {
            updateMask(canvas.width, canvas.height)
        }

        offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)
        canvas.drawBitmap(offscreenBitmap, 0f, 0f, paint)
    }

    private fun updateMask(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(mask)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRect(rect, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(rect, borderRadius, borderRadius, paint)

        this.maskBitmap = mask
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateMask(width, height)
    }
}