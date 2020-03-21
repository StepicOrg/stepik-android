package org.stepic.droid.ui.custom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R
import org.stepic.droid.util.resolveColorAttribute
import org.stepic.droid.util.resolveFloatAttribute
import org.stepik.android.view.base.ui.extension.ColorExtensions

class LoadingView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val baseColorDefault = context.resolveColorAttribute(R.attr.colorControlHighlight)
    private val deepColorDefault = ColorExtensions.colorWithAlphaMul(context.resolveColorAttribute(R.attr.colorControlHighlight), context.resolveFloatAttribute(R.attr.alphaEmphasisDisabled))
    private val durationDefault = 1500L
    private val intervalDefault = 0L

    private val animator: ValueAnimator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                frame = it.animatedFraction
                postInvalidate()
            }
        }

    private var frame = 0f
    private var radius: Float = 0f
    private var progressLength: Float = 0f
    private var baseColor = baseColorDefault
    private var deepColor = deepColorDefault
    private var durationOfPass = durationDefault
    private var interval = intervalDefault

    private var basePaint: Paint
    private var deepPaintLeft: Paint
    private var deepPaintRight: Paint

    private var rect = RectF()
    private var path = Path()

    private val screenWidth: Int by lazy {
        context.resources.displayMetrics.widthPixels
    }

    private var localMatrix: Matrix = Matrix()

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        try {
            with(array) {
                radius = getDimensionPixelOffset(R.styleable.LoadingView_radius, resources.getDimensionPixelOffset(R.dimen.loading_view_radius_default)).toFloat()
                durationOfPass = getInt(R.styleable.LoadingView_duration, durationDefault.toInt()).toLong()
                interval = getInt(R.styleable.LoadingView_interval, intervalDefault.toInt()).toLong()
                baseColor = getColor(R.styleable.LoadingView_baseColor, baseColorDefault)
                deepColor = getColor(R.styleable.LoadingView_deepColor, deepColorDefault)
                progressLength = getDimensionPixelOffset(R.styleable.LoadingView_progressLength, resources.getDimensionPixelOffset(R.dimen.loading_view_progress_length_default)).toFloat()
            }
        } finally {
            array.recycle()
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //we use clipPath in onDraw see https://stackoverflow.com/a/8895894/5222184
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }

        basePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = baseColor
        }
        deepPaintLeft = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, baseColor, deepColor, Shader.TileMode.CLAMP)
        }
        deepPaintRight = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, deepColor, baseColor, Shader.TileMode.CLAMP)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.clipPath(path.apply { reset(); addRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, Path.Direction.CW) })
        super.onDraw(canvas)
        canvas.drawRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, basePaint)

        canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x, 0f, screenWidth * frame - x + progressLength / 2, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x, 0f) }) })
        canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2, 0f, screenWidth * frame - x + progressLength, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x + progressLength / 2, 0f) }) })

        if (screenWidth - (screenWidth * frame + progressLength) < 0) {
            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x - screenWidth, 0f, screenWidth * frame - x + progressLength / 2 - screenWidth, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x - screenWidth, 0f) }) })
            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f, screenWidth * frame - x + progressLength - screenWidth, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f) }) })
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }


    @SuppressLint("SwitchIntDef")
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        //tbh, it may produce the unexpected start (View1->View2->thisView)
        //View2 - Gone
        //View1 – Visible
        //but it is better than remain this view unstoppable
        //animator is stopped in onDetachedFromWindow also
        when (visibility) {
            View.VISIBLE -> {
                start()
            }
            View.GONE, View.INVISIBLE -> {
                stop()
            }
            else -> {
                throw IllegalStateException("Visibility should be one of VISIBLE, INVISIBLE, GONE")
            }
        }
    }

    private fun start() =
            with(animator) {
                duration = durationOfPass
                startDelay = interval
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

    private fun stop() = with(animator) { if (isRunning) cancel() }
}
