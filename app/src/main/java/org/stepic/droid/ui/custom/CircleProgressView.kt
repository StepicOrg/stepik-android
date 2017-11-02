package org.stepic.droid.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DpPixelsHelper

class CircleProgressView
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : View(context, attributeSet, defStyleAttr, defStyleRes) {

    companion object {
        private const val MAX_DEGREES = 360f
    }

    var progress: Float = 0.0f

    //Rectangles
    private val oval = RectF()

    //Paints
    private val backgroundPaint = Paint()
    private val foregroundPaint = Paint()


    //Colors (with defaults)
    @ColorInt
    private var backgroundPaintColor = ColorUtil.getColorArgb(R.color.new_accent_color, context)
    @ColorInt
    private var foregroundPaintColor = ColorUtil.getColorArgb(R.color.stepic_brand_primary, context)

    private var strokeWidth = DpPixelsHelper.convertDpToPixel(2f)

    init {
        parseAttributes(context.obtainStyledAttributes(attributeSet, R.styleable.CircleProgressView))

        setupPaint(backgroundPaint, backgroundPaintColor)
        setupPaint(foregroundPaint, foregroundPaintColor)

    }

    /**
     * Parse the attributes passed to the view from the XML
     *
     * @param a the attributes to parse
     */
    private fun parseAttributes(a: TypedArray) {
        try {
            backgroundPaintColor = a.getColor(R.styleable.CircleProgressView_progressBackgroundColor, backgroundPaintColor)
            foregroundPaintColor = a.getColor(R.styleable.CircleProgressView_progressForegroundColor, foregroundPaintColor)
            strokeWidth = a.getDimension(R.styleable.CircleProgressView_progressBarStroke, strokeWidth)
        } finally {
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setupBounds(w, h)
    }

    private fun setupBounds(layoutWidth: Int, layoutHeight: Int) {
        oval.set(paddingLeft + strokeWidth,
                paddingTop + strokeWidth,
                layoutWidth - paddingRight - strokeWidth,
                layoutHeight - paddingBottom - strokeWidth)
    }

    private fun setupPaint(paint: Paint, paintColor: Int) {
        paint.color = paintColor
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(oval, MAX_DEGREES, MAX_DEGREES, false, backgroundPaint)
        val progressDegrees = progress * MAX_DEGREES
        canvas.drawArc(oval, -90F, progressDegrees, false, foregroundPaint)
    }

}
