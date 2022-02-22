package org.stepik.android.view.course_list.ui.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import org.stepic.droid.R
import ru.nobird.android.view.base.ui.extension.dp
import ru.nobird.android.view.base.ui.extension.sp
import ru.nobird.android.view.base.ui.extension.toPx

class CertificateProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defAttrStyle: Int = 0) : View(context, attrs, defAttrStyle) {

    private val labelTextSize = 12f.sp.toPx()
    private val progressLabelBottomMargin = 4.dp.toPx()
    private val certificateLabelBottomMargin = 12.dp.toPx()
    private val progressBarThickness = 2.dp.toPx()
    private val progressBarRadius = 4.dp.toPx()
    private val labelRadius = 8.dp.toPx()
    private val labelInnerPadding = 4.dp.toPx()
    private val thresholdCheckedRadius = 4.5f.dp.toPx()
    private val thresholdUncheckRadius = 2.5f.dp.toPx()

    private val regularCertificateColor = ContextCompat.getColor(context, R.color.color_overlay_green)
    private val distinctCertificateColor = ContextCompat.getColor(context, R.color.color_overlay_yellow)

    private val regularDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_certificate_regular)
    private val distinctDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_certificate_distinct)
    private val checkmarkDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_checkmark)

    private val labelDrawableWidth = regularDrawable?.intrinsicWidth ?: 0

    private var progressText: String = ""
    private var progressTextWidth: Float = 0.0f

    var state: State = State.Idle // State.HasBoth(115f, 116L, 100, 115)
        set(value) {
            field = value
            progressText = "${state.currentProgress.toInt()} / ${state.cost}"
            progressTextWidth = progressTextPaint.measureText(progressText)
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
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(canvas, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())
            }

            is State.HasRegular -> {
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(canvas, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                handleRegularCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.regularThreshold)
                handleProgressIntersection(regularLabelBounds, progressTextPaint.measureText(progressText))
                drawLabel(canvas, regularDrawable, currentState.regularThreshold.toString(), regularLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, regularLabelBounds.centerY(), regularLabelPaint, regularLabelTextPaint, regularLabelBounds)
            }

            is State.HasDistinct -> {
                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), distinctProgressbarPaint)
                drawProgressLabel(canvas, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                handleDistinctCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.distinctThreshold)
                handleProgressIntersection(distinctLabelBounds, progressTextPaint.measureText(progressText))
                drawLabel(canvas, distinctDrawable, currentState.distinctThreshold.toString(), distinctLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, distinctLabelBounds.centerY(), distinctLabelPaint, distinctLabelTextPaint, distinctLabelBounds)
            }
            is State.HasBoth -> {
                val progress = if (currentState.currentProgress >= currentState.distinctThreshold) {
                    currentState.distinctThreshold
                } else {
                    currentState.currentProgress
                }

                drawProgressbar(canvas, currentState.cost.toFloat(), currentState.cost.toFloat(), initialProgressBarPaint)
                drawProgressbar(canvas, currentState.currentProgress, currentState.cost.toFloat(), distinctProgressbarPaint)
                drawProgressbar(canvas, progress.toFloat(), currentState.cost.toFloat(), regularProgressBarPaint)
                drawProgressLabel(canvas, paddingLeft, (bottom - progressBarThickness.value - paddingBottom - progressLabelBottomMargin.value).toInt())

                handleRegularCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.regularThreshold)
                handleDistinctCertificate(canvas, currentState.currentProgress, currentState.cost, currentState.distinctThreshold)

                handleProgressIntersection(regularLabelBounds, progressTextWidth)
                handleProgressIntersection(distinctLabelBounds, progressTextWidth)
                handleLabelsIntersection(regularLabelBounds, distinctLabelBounds)
                drawLabel(canvas, regularDrawable, currentState.regularThreshold.toString(), regularLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, regularLabelBounds.centerY(), regularLabelPaint, regularLabelTextPaint, regularLabelBounds)
                drawLabel(canvas, distinctDrawable, currentState.distinctThreshold.toString(), distinctLabelBounds.centerX() + (paddingLeft + paddingRight) / 2, distinctLabelBounds.centerY(), distinctLabelPaint, distinctLabelTextPaint, distinctLabelBounds)
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

        if (labelBounds.right > width - paddingRight) {
            labelBounds.set(
                width - paddingRight - labelBounds.width(),
                labelBounds.top,
                width - paddingRight,
                labelBounds.bottom
            )
        }
    }

    private fun handleLabelsIntersection(regularLabel: Rect, distinctLabel: Rect) {
        if (Rect.intersects(regularLabel, distinctLabel)) {
            val tryLeftRegularX =
                (distinctLabel.left - regularLabel.width()) - 2.dp.toPx().value

            val tryRightDistinctX =
                distinctLabel.right + (regularLabel.right - distinctLabel.left)

            if (tryLeftRegularX > progressTextBounds.right) {
                regularLabel.set(
                    tryLeftRegularX.toInt(),
                    regularLabel.top,
                    distinctLabel.left,
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

    private fun drawProgressLabel(canvas: Canvas, x: Int, y: Int) {
        canvas.drawText(progressText, x.toFloat(), y.toFloat(), progressTextPaint)
        progressTextBounds.set(
            paddingLeft,
            y - 40,
            progressTextWidth.toInt() + paddingLeft,
            y - 20
        )
    }

    private fun calculateLabelBounds(labelText: String, x: Int, y: Int, textPaint: Paint, labelBounds: Rect) {
        val totalWidth = labelDrawableWidth + textPaint.measureText(labelText)
        textPaint.getTextBounds(labelText, 0, labelText.length, textBoundsRect)
        val textHeight = textBoundsRect.height()

        labelBounds.set(
            (x.toFloat() - (totalWidth / 2f) - labelInnerPadding.value - (regularDrawable?.intrinsicWidth ?: 0)).toInt(),
            (y.toFloat() - (textHeight / 2f) - labelInnerPadding.value).toInt(),
            (x + (totalWidth / 2f) + labelInnerPadding.value).toInt(),
            (y + (textHeight / 2f) + labelInnerPadding.value).toInt()
        )
    }

    private fun drawLabel(canvas: Canvas, labelDrawable: Drawable?, labelText: String, x: Int, y: Int, labelPaint: Paint, textPaint: Paint, labelBounds: Rect) {
        calculateLabelBounds(labelText, x, y, textPaint, labelBounds)
        canvas.drawRoundRect(
            labelBounds.left.toFloat(),
            labelBounds.top.toFloat(),
            labelBounds.right.toFloat(),
            labelBounds.bottom.toFloat(),
            labelRadius.value,
            labelRadius.value,
            labelPaint
        )
        textPaint.getTextBounds(labelText, 0, labelText.length, textBoundsRect)
        val textWidth = textPaint.measureText(labelText)

        labelDrawable?.setBounds(
            (labelBounds.left + labelInnerPadding.value * 1.5).toInt(),
            (y - labelDrawable.intrinsicWidth / 2),
            ((labelBounds.left + labelInnerPadding.value * 1.5) + labelDrawable.intrinsicWidth * 1.1).toInt(),
            (y - labelDrawable.intrinsicWidth / 2 + (labelDrawable.intrinsicHeight * 1.1)).toInt()
        )
        labelDrawable?.draw(canvas)
        canvas.drawText(labelText, x - (textWidth / 2f), y.toFloat() + labelBounds.height() / 4, textPaint)
    }

    private fun drawThresholdIcon(canvas: Canvas, thresholdAchieved: Boolean, x: Int, y: Int, paint: Paint) {
        if (thresholdAchieved) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), thresholdCheckedRadius.value, paint)
            checkmarkDrawable?.setBounds(
                x - checkmarkDrawable.intrinsicWidth / 2,
                y - checkmarkDrawable.intrinsicWidth / 2,
                x + checkmarkDrawable.intrinsicWidth / 2,
                y + checkmarkDrawable.intrinsicWidth / 2
            )
            checkmarkDrawable?.draw(canvas)
        } else {
            canvas.drawCircle(x.toFloat(), y.toFloat(), thresholdUncheckRadius.value, paint)
        }
    }

    private fun drawProgressbar(canvas: Canvas, progress: Float, max: Float, paint: Paint) {
        val right = progress * (width.toFloat() - (paddingLeft + paddingRight)) / max
        canvas.drawRoundRect(
            paddingLeft.toFloat(),
            bottom - progressBarThickness.value - paddingBottom,
            right + paddingLeft,
            (bottom - paddingBottom).toFloat(),
            progressBarRadius.value,
            progressBarRadius.value,
            paint
        )
    }

    sealed class State(val currentProgress: Float, val cost: Long) {
        object Idle : State(0f, 0)
        class NoCertificate(currentProgress: Float, cost: Long) : State(currentProgress, cost)
        class HasRegular(currentProgress: Float, cost: Long, val regularThreshold: Long) : State(currentProgress, cost)
        class HasDistinct(currentProgress: Float, cost: Long, val distinctThreshold: Long) : State(currentProgress, cost)
        class HasBoth(
            currentProgress: Float,
            cost: Long,
            val regularThreshold: Long,
            val distinctThreshold: Long
        ) : State(currentProgress, cost)
    }
}