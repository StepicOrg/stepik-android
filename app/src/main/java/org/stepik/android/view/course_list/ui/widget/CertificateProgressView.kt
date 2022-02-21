package org.stepik.android.view.course_list.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Canvas
import android.graphics.RectF
import androidx.core.graphics.toRectF
import org.stepic.droid.R
import ru.nobird.android.view.base.ui.extension.dp
import ru.nobird.android.view.base.ui.extension.sp
import ru.nobird.android.view.base.ui.extension.toPx

class CertificateProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defAttrStyle: Int = 0) : View(context, attrs, defAttrStyle) {

    private val labelTextSize = 12f.sp.toPx()
    private val progressLabelBottomMargin = 4.dp.toPx()
    private val certificateLabelBottomMargin = 16.dp.toPx()
    private val progressBarThickness = 2.dp.toPx()

    private val regularCertificateColor = ContextCompat.getColor(context, R.color.color_overlay_green)
    private val distinctCertificateColor = ContextCompat.getColor(context, R.color.color_overlay_yellow)

    private val regularDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_certificate_regular)
    private val distinctDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_certificate_distinct)
    private val checkmarkDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_checkmark)

    private val labelDrawableWidth = regularDrawable?.intrinsicWidth ?: 0

    var state: State = State.Idle // State.HasBoth(125f, 200L, 115, 182) - Can be used to test corner-cases
        set(value) {
            field = value
            invalidate()
        }

    private val initialProgressBarPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.color_on_surface_alpha_12)
        style = Paint.Style.FILL_AND_STROKE
    }

    private val regularProgressBarPaint = Paint().apply {
        isAntiAlias = true
        color = regularCertificateColor
        style = Paint.Style.FILL_AND_STROKE
    }

    private val regularLabelPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.color_overlay_green_alpha_12)
        style = Paint.Style.FILL
    }

    private val regularLabelTextPaint = Paint().apply {
        isAntiAlias = true
        color = regularCertificateColor
        style = Paint.Style.FILL
        textSize = labelTextSize.value
    }

    private val distinctProgressbarPaint = Paint().apply {
        isAntiAlias = true
        color = distinctCertificateColor
        style = Paint.Style.FILL_AND_STROKE
    }

    private val distinctLabelPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.color_overlay_yellow_alpha_12)
        style = Paint.Style.FILL
    }

    private val distinctLabelTextPaint = Paint().apply {
        isAntiAlias = true
        color = distinctCertificateColor
        style = Paint.Style.FILL
        textSize = labelTextSize.value
    }

    private val progressTextPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.color_primary)
        textSize = labelTextSize.value
    }

    private val progressTextBounds = Rect()

    private val regularLabelBounds = Rect()
    private val distinctLabelBounds = Rect()

    private val textBoundsRect = Rect()

    override fun onDraw(canvas: Canvas) {
        when (val currentState = state) {
            is State.Idle -> {}
            is State.NoCertificate -> {
                val progressText = "${currentState.currentProgress.toInt()} / ${currentState.cost}"
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(
                    canvas,
                    progressText,
                    paddingLeft,
                    (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt()
                )
            }

            is State.HasRegular -> {
                val progressText = "${currentState.currentProgress.toInt()} / ${currentState.cost}"
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(canvas, progressText, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                val regularThresholdX = resolveThresholdX(currentState.regularThreshold.toFloat(), currentState.cost.toFloat())
                val regularText = currentState.regularThreshold.toString()

                drawThresholdIcon(canvas, currentState.currentProgress >= currentState.regularThreshold, regularThresholdX, (bottom - (progressBarThickness.value / 2) - paddingBottom).toInt(), regularProgressBarPaint)
                calculateLabelBounds(regularText, regularThresholdX, bottom - paddingBottom - certificateLabelBottomMargin.value.toInt(), regularLabelTextPaint, regularLabelBounds)
                handleProgressIntersection(regularLabelBounds, progressTextPaint.measureText(progressText))
                drawLabel(canvas, regularDrawable, regularText, regularLabelBounds.centerX(), regularLabelBounds.centerY() + 8, regularLabelPaint, regularLabelTextPaint, regularLabelBounds)
            }

            is State.HasDistinct -> {
                val progressText = "${currentState.currentProgress.toInt()} / ${currentState.cost}"
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), distinctProgressbarPaint)
                drawProgressLabel(canvas, progressText, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                val distinctThresholdX = resolveThresholdX(currentState.distinctThreshold.toFloat(), currentState.cost.toFloat())
                val distinctText = currentState.distinctThreshold.toString()

                drawThresholdIcon(canvas, currentState.currentProgress >= currentState.distinctThreshold, distinctThresholdX, (bottom - (progressBarThickness.value / 2) - paddingBottom).toInt(), distinctProgressbarPaint)
                calculateLabelBounds(distinctText, distinctThresholdX, bottom - paddingBottom - certificateLabelBottomMargin.value.toInt(), distinctLabelTextPaint, distinctLabelBounds)
                handleProgressIntersection(distinctLabelBounds, progressTextPaint.measureText(progressText))
                drawLabel(canvas, distinctDrawable, distinctText, distinctLabelBounds.centerX(), distinctLabelBounds.centerY() + 8, distinctLabelPaint, distinctLabelTextPaint, distinctLabelBounds)
            }
            is State.HasBoth -> {
                val progressText = "${currentState.currentProgress.toInt()}/ ${currentState.cost}"
                val progress = if (currentState.currentProgress >= currentState.regularThreshold) {
                    currentState.regularThreshold
                } else {
                    currentState.currentProgress
                }

                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), distinctProgressbarPaint)
                drawProgressbar(canvas, progress.toFloat(), currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(canvas, progressText, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                handleRegularCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.regularThreshold)
                handleDistinctCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.distinctThreshold)

                val progressTextWidth = progressTextPaint.measureText(progressText)
                handleProgressIntersection(regularLabelBounds, progressTextWidth)
                handleProgressIntersection(distinctLabelBounds, progressTextWidth)
                handleLabelsIntersection(regularLabelBounds, distinctLabelBounds)
                drawLabel(canvas, regularDrawable, currentState.regularThreshold.toString(), regularLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, regularLabelBounds.centerY() + 8, regularLabelPaint, regularLabelTextPaint, regularLabelBounds)
                drawLabel(canvas, distinctDrawable, currentState.distinctThreshold.toString(), distinctLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, distinctLabelBounds.centerY() + 8, distinctLabelPaint, distinctLabelTextPaint, distinctLabelBounds)
            }
        }
    }

    private fun handleRegularCertificate(canvas: Canvas, currentProgress: Float, cost: Long, regularThreshold: Long) {
        val regularThresholdX = resolveThresholdX(regularThreshold.toFloat(), cost.toFloat())
        drawThresholdIcon(
            canvas,
            currentProgress >= regularThreshold,
            regularThresholdX,
            (bottom - (progressBarThickness.value / 2) - paddingBottom).toInt(),
            regularProgressBarPaint
        )
        calculateLabelBounds(regularThreshold.toString(), regularThresholdX, bottom - paddingBottom - certificateLabelBottomMargin.value.toInt(), regularLabelTextPaint, regularLabelBounds)
    }

    private fun handleDistinctCertificate(canvas: Canvas, currentProgress: Float, cost: Long, distinctThreshold: Long) {
        val distinctThresholdX = resolveThresholdX(distinctThreshold.toFloat(), cost.toFloat())
        drawThresholdIcon(
            canvas,
            currentProgress >= distinctThreshold,
            distinctThresholdX,
            (bottom - (progressBarThickness.value / 2) - paddingBottom).toInt(),
            distinctProgressbarPaint
        )
        calculateLabelBounds(distinctThreshold.toString(), distinctThresholdX, bottom - paddingBottom - certificateLabelBottomMargin.value.toInt(), distinctLabelTextPaint, distinctLabelBounds)
    }

    private fun handleProgressIntersection(labelBounds: Rect, progressTextWidth: Float) {
        if (Rect.intersects(progressTextBounds, labelBounds)) {
            labelBounds.set(
                progressTextBounds.right,
                labelBounds.top,
                progressTextWidth.toInt() + labelBounds.width() + paddingLeft,
                labelBounds.bottom
            )
        }

        if (labelBounds.right > width - paddingLeft - paddingRight) {
            labelBounds.set(
                width - labelBounds.width(),
                labelBounds.top,
                width - paddingRight,
                labelBounds.bottom
            )
        }
    }

    private fun handleLabelsIntersection(regularLabel: Rect, distinctLabel: Rect) {
        if (Rect.intersects(regularLabel, distinctLabel)) {
            val tryLeftRegularX =
                regularLabel.left - (regularLabel.right - distinctLabel.left)
            val tryRightDistinctX =
                distinctLabel.right + (regularLabel.right - distinctLabel.left)

            if (tryLeftRegularX > progressTextBounds.right) {
                regularLabel.set(
                    regularLabel.left - (regularLabel.right - distinctLabel.left),
                    regularLabel.top,
                    distinctLabel.left - paddingRight,
                    regularLabel.bottom
                )
            } else if (tryRightDistinctX < width - paddingRight) {
                distinctLabel.set(
                    regularLabel.right + paddingRight,
                    distinctLabel.top,
                    tryRightDistinctX,
                    distinctLabel.bottom
                )
            }
        }
    }

    private fun resolveThresholdX(threshold: Float, max: Float): Int {
        val thresholdX = threshold * (width.toFloat() - (paddingLeft + paddingRight)) / max
        return (thresholdX + paddingLeft).toInt()
    }

    private fun drawProgressLabel(canvas: Canvas, progressText: String, x: Int, y: Int) {
        val progressTextWidth = progressTextPaint.measureText(progressText)
        canvas.drawText(progressText, x.toFloat(), y.toFloat(), progressTextPaint)
        progressTextBounds.set(
            0 + paddingLeft,
            y - 40,
            progressTextWidth.toInt() + paddingLeft,
            y - 20
        )
    }

    private fun calculateLabelBounds(labelText: String, x: Int, y: Int, textPaint: Paint, labelBounds: Rect) {
        val totalWidth = labelDrawableWidth + textPaint.measureText(labelText)
        textPaint.getTextBounds(labelText, 0, labelText.length, textBoundsRect)
        val textHeight = textBoundsRect.height()

        val padding = 8f // Padding around icon and text
        labelBounds.set(
            (x.toFloat() - (totalWidth / 2f) - padding - (regularDrawable?.intrinsicWidth ?: 0)).toInt(),
            (y.toFloat() - (textHeight / 2f) - padding).toInt(),
            (x + (totalWidth / 2f) + padding).toInt(),
            (y + (textHeight / 2f) + padding).toInt()
        )
    }

    private fun drawLabel(canvas: Canvas, labelDrawable: Drawable?, labelText: String, x: Int, y: Int, labelPaint: Paint, textPaint: Paint, labelBounds: Rect) {
        calculateLabelBounds(labelText, x, y, textPaint, labelBounds)
        canvas.drawRoundRect(
            labelBounds.toRectF(),
            20f,
            20f,
            labelPaint
        )
        val totalWidth = labelDrawableWidth + textPaint.measureText(labelText)
        textPaint.getTextBounds(labelText, 0, labelText.length, textBoundsRect)
        val textHeight = textBoundsRect.height()
        val textWidth = textPaint.measureText(labelText)

        val padding = 8 // Padding around icon and text
        labelDrawable?.setBounds(
            (x.toFloat() - (totalWidth / 2f) - padding * 2).toInt(),
            (y.toFloat() - (textHeight / 2f) + 2).toInt(),
            ((x.toFloat() - (totalWidth / 2f) - padding * 2) + labelDrawable.intrinsicWidth).toInt(),
            (y.toFloat() - (textHeight / 2f) + 2 + labelDrawable.intrinsicHeight).toInt()
        )
        labelDrawable?.draw(canvas)
        canvas.drawText(labelText, x - (textWidth / 2f), y + (textHeight / 2f), textPaint)
    }

    private fun drawThresholdIcon(canvas: Canvas, thresholdAchieved: Boolean, x: Int, y: Int, paint: Paint) {
        if (thresholdAchieved) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), 12f, paint)
            checkmarkDrawable?.setBounds(
                x - checkmarkDrawable.intrinsicWidth / 2,
                y - checkmarkDrawable.intrinsicWidth / 2,
                x + checkmarkDrawable.intrinsicWidth / 2,
                y + checkmarkDrawable.intrinsicWidth / 2
            )
            checkmarkDrawable?.draw(canvas)
        } else {
            canvas.drawCircle(x.toFloat(), y.toFloat(), 8f, paint)
        }
    }

    private fun drawProgressbar(canvas: Canvas, progress: Float, max: Float, paint: Paint) {
        val right = progress * (width.toFloat() - (paddingLeft + paddingRight)) / max
        val rect = RectF(
            paddingLeft.toFloat(),
            bottom - progressBarThickness.value - paddingBottom,
            right + paddingLeft,
            (bottom - paddingBottom).toFloat()
        )
        canvas.drawRoundRect(rect, 4f, 4f, paint)
    }

    sealed class State {
        object Idle : State()
        data class NoCertificate(val currentProgress: Float, val cost: Long) : State()
        data class HasRegular(val currentProgress: Float, val cost: Long, val regularThreshold: Long) : State()
        data class HasDistinct(val currentProgress: Float, val cost: Long, val distinctThreshold: Long) : State()
        data class HasBoth(
            val currentProgress: Float,
            val cost: Long,
            val regularThreshold: Long,
            val distinctThreshold: Long
        ) : State()
    }
}