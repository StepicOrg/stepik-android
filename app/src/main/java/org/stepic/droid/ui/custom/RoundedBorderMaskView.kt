package org.stepic.droid.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.ViewOutlineProvider
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
    private var backgroundDrawable: GradientDrawable? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedBorderMaskView)
        try {
            borderRadius = attributes.getDimension(R.styleable.RoundedBorderMaskView_borderRadius, 0f)
        } finally {
            attributes.recycle()
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = ViewOutlineProvider.BACKGROUND
            clipToOutline = true

            val drawable = GradientDrawable()
            drawable.cornerRadius = borderRadius
            background = drawable
            backgroundDrawable = drawable
        } else {
            maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        maskBitmap?.let { canvas.drawBitmap(it, 0f, 0f, maskPaint) }
    }

    private fun updateMask(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            backgroundDrawable?.cornerRadius = borderRadius
            backgroundDrawable?.let(::setBackgroundDrawable)
        } else {
            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(mask)

            canvas.drawRoundRect(rect, borderRadius, borderRadius, antiAliasedPaint)

            this.maskBitmap = mask
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateMask(width, height)
    }
}