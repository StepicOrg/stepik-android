package org.stepic.droid.ui.custom.vertical_progress_bar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R

class VerticalProgressBar
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val rect = Rect()
    private val paint = Paint()

    @ColorInt
    var progressBackgroundColor: Int

    @ColorInt
    var progressForegroundColor: Int

    var progress = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalProgressBar)

        try {
            progressBackgroundColor = typedArray.getColor(R.styleable.VerticalProgressBar_progressBackgroundColor, 0)
            progressForegroundColor = typedArray.getColor(R.styleable.VerticalProgressBar_progressForegroundColor, 0)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val offset = height * (1f - progress).toInt()

        paint.color = progressBackgroundColor
        rect.set(0, 0, width, offset)
        canvas.drawRect(rect, paint)

        paint.color = progressForegroundColor
        rect.set(0, offset, width, height)
        canvas.drawRect(rect, paint)
    }
}