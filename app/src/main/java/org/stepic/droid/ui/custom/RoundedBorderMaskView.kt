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

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val antiAliasedPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var maskBitmap: Bitmap? = null
    private var bufferBitmap: Bitmap? = null
    private var bufferCanvas: Canvas? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedBorderMaskView)
        try {
            borderRadius = attributes.getDimension(R.styleable.RoundedBorderMaskView_borderRadius, 0f)
        } finally {
            attributes.recycle()
        }

        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        super.draw(bufferCanvas)
        bufferCanvas?.drawBitmap(maskBitmap, 0f, 0f, maskPaint)
        canvas.drawBitmap(bufferBitmap, 0f, 0f, null)
    }

    private fun updateMask(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bufferCanvas = Canvas(bufferBitmap)

        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mask)

        canvas.drawRoundRect(rect, borderRadius, borderRadius, antiAliasedPaint)

        this.maskBitmap = mask
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateMask(width, height)
    }
}