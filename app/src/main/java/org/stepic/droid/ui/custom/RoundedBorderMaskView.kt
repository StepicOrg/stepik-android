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

// TODO APPS-3146 Will be removed with new design of FastContinue
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

        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true

        val drawable = GradientDrawable()
        drawable.cornerRadius = borderRadius
        background = drawable
        backgroundDrawable = drawable
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        maskBitmap?.let { canvas.drawBitmap(it, 0f, 0f, maskPaint) }
    }

    private fun updateMask(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        backgroundDrawable?.cornerRadius = borderRadius
        backgroundDrawable?.let(::setBackgroundDrawable)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateMask(width, height)
    }
}