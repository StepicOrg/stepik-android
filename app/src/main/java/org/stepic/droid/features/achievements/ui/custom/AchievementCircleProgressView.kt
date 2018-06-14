package org.stepic.droid.features.achievements.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R

class AchievementCircleProgressView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val backgroundAlpha = 0x3FFFFFFF
    private val backgroundColors = context.resources.getIntArray(R.array.achievements_circle_progress_background_colors).map { it and backgroundAlpha }.toIntArray()
    private val backgroundColorsPositions = floatArrayOf(0f, 0.1f, 0.2f, 0.45f, 1f)

    private var arcRect: RectF = RectF()

    private val strokeWidth: Float
    private val progressColor: Int

    var progress: Float

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AchievementCircleProgressView)
        try {
            strokeWidth = attributes.getDimension(R.styleable.AchievementCircleProgressView_strokeWidth, 3f)
            progressColor = attributes.getColor(R.styleable.AchievementCircleProgressView_progressColor, 0)
            progress = attributes.getFloat(R.styleable.AchievementCircleProgressView_progress, 0f)
        } finally {
            attributes.recycle()
        }
    }


    private val backgroundPaint = Paint().apply {
        flags = flags or Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        strokeWidth = this@AchievementCircleProgressView.strokeWidth
    }

    private val progressPaint = Paint().apply {
        flags = flags or Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        strokeWidth = this@AchievementCircleProgressView.strokeWidth
        color = progressColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, width / 2 - strokeWidth / 2, backgroundPaint)
        canvas.drawArc(arcRect, 90f, progress * 360, false, progressPaint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        backgroundPaint.shader = LinearGradient(width.toFloat(), height.toFloat(), 0f, 0f, backgroundColors, backgroundColorsPositions, Shader.TileMode.MIRROR)
        arcRect = RectF(strokeWidth / 2, strokeWidth / 2, width - strokeWidth / 2, height - strokeWidth / 2)
    }
}