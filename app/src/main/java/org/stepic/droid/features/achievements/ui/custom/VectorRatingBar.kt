package org.stepic.droid.features.achievements.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class VectorRatingBar
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private lateinit var progressBitmap: Bitmap
    private lateinit var secondaryProgressBitmap: Bitmap
    private lateinit var backgroundBitmap: Bitmap

    @DrawableRes var progressRes: Int
    @DrawableRes var secondaryProgressRes: Int
    @DrawableRes var backgroundRes: Int

    var progress: Int by Delegates.observable(0) { _, _, _ -> requestLayout() }
    var total: Int by Delegates.observable(0) { _, _, _ -> requestLayout() }
    var gap: Float by Delegates.observable(0f) { _, _, _ -> requestLayout() }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.VectorRatingBar)

        try {
            backgroundRes = attributes.getResourceId(R.styleable.VectorRatingBar_backgroundIcon, android.R.drawable.star_off)
            secondaryProgressRes = attributes.getResourceId(R.styleable.VectorRatingBar_secondaryIcon, android.R.drawable.star_on)
            progressRes = attributes.getResourceId(R.styleable.VectorRatingBar_progressIcon, android.R.drawable.star_off)

            progress = attributes.getInteger(R.styleable.VectorRatingBar_currentProgress, 0)
            total = attributes.getInteger(R.styleable.VectorRatingBar_totalProgress, 0)
            gap = attributes.getDimension(R.styleable.VectorRatingBar_itemsGap, 0f)
        } finally {
            attributes.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        var offset = 0f
        for (i in 0 until min(progress, total)) {
            canvas.drawBitmap(progressBitmap, offset, 0f, null)
            offset += progressBitmap.width + gap
        }

        if (progress < total) {
            canvas.drawBitmap(secondaryProgressBitmap, offset, 0f, null)
            offset += secondaryProgressBitmap.width + gap
        }

        for (i in progress + 1 until total) {
            canvas.drawBitmap(backgroundBitmap, offset, 0f, null)
            offset += backgroundBitmap.width + gap
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        progressBitmap = getBitmap(progressRes, heightMode, heightSize)
        secondaryProgressBitmap = getBitmap(secondaryProgressRes, heightMode, heightSize)
        backgroundBitmap = getBitmap(backgroundRes, heightMode, heightSize)

        val height = maxOf(progressBitmap.height, secondaryProgressBitmap.height, backgroundBitmap.height)

        val totalWidth = progressBitmap.width * min(total, progress) +
                secondaryProgressBitmap.width * min(1, max(0, total - progress)) +
                backgroundBitmap.width * max(0, total - progress - 1) +
                (gap * (total - 1)).toInt()

        @SuppressLint("SwitchIntDef")
        val width = when(widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(widthSize, totalWidth)
            else -> totalWidth
        }

        setMeasuredDimension(width, height)
    }

    private fun getBitmap(@DrawableRes resId: Int, heightMode: Int, heightSize: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, resId)!!

        val targetHeight = when(heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(drawable.intrinsicHeight, heightSize)
            else -> drawable.intrinsicHeight
        }

        val targetWidth = (drawable.intrinsicWidth * (targetHeight.toFloat() / drawable.intrinsicHeight)).toInt()

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}