package org.stepik.android.view.latex.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
import org.stepic.droid.R
import org.stepic.droid.util.resolveColorAttribute
import org.stepic.droid.util.resolveFloatAttribute
import org.stepic.droid.util.toSp
import org.stepik.android.view.base.ui.extension.ColorExtensions

data class TextAttributes(
    val textSize: Float,
    @ColorInt
    val textColor: Int,
    @ColorInt
    val textColorHighlight: Int,
    val textIsSelectable: Boolean,

    @FontRes
    val fontResId: Int,

    val isNightMode: Boolean
) {
    companion object {
        private val attrsSet =
            intArrayOf(
                android.R.attr.textSize,
                android.R.attr.textColor,
                android.R.attr.textColorHighlight,
                android.R.attr.textIsSelectable,
                R.attr.fontFamily,
                R.attr.isNightMode
            ).apply { sort() }

        @SuppressLint("ResourceType")
        fun fromAttributeSet(context: Context, attrs: AttributeSet?): TextAttributes {
            val colorOnSurface = context.resolveColorAttribute(R.attr.colorOnSurface)
            val alphaEmphasisHigh = context.resolveFloatAttribute(R.attr.alphaEmphasisHigh)

            // default params
            var textAttributes = TextAttributes(
                textSize = 14f,
                textColor = ColorExtensions.colorWithAlphaMul(colorOnSurface, alphaEmphasisHigh),
                textColorHighlight = context.resolveColorAttribute(android.R.attr.textColorHighlight),
                textIsSelectable = false,
                fontResId = R.font.roboto_regular,
                isNightMode = false
            )

            val textAppearance = obtainTextAppearance(context, attrs)
            if (textAppearance != null) {
                textAttributes = readTypedArray(textAttributes, textAppearance)
            }

            val array = context.obtainStyledAttributes(attrs, attrsSet)
            textAttributes = readTypedArray(textAttributes, array)

            return textAttributes
        }

        private fun obtainTextAppearance(context: Context, attrs: AttributeSet?): TypedArray? {
            val a = context.theme.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textAppearance), -1, -1)
            val ap = a.getResourceId(0, -1)
            a.recycle()

            return if (ap != -1) {
                context.theme.obtainStyledAttributes(ap, attrsSet)
            } else {
                null
            }
        }

        private fun readTypedArray(textAttributes: TextAttributes, array: TypedArray): TextAttributes =
            try {
                TextAttributes(
                    textSize =
                        array.getDimensionPixelSize(attrsSet.indexOf(android.R.attr.textSize), 0)
                            .takeIf { it > 0 }
                            ?.toFloat()
                            ?.toSp()
                            ?: textAttributes.textSize,

                    textColor =
                        array.getColor(attrsSet.indexOf(android.R.attr.textColor), textAttributes.textColor),

                    textColorHighlight =
                        array.getColor(attrsSet.indexOf(android.R.attr.textColorHighlight), textAttributes.textColorHighlight),

                    textIsSelectable =
                        array.getBoolean(attrsSet.indexOf(android.R.attr.textIsSelectable), textAttributes.textIsSelectable),

                    fontResId =
                        array.getResourceId(attrsSet.indexOf(R.attr.fontFamily), textAttributes.fontResId),

                    isNightMode =
                        array.getBoolean(attrsSet.indexOf(R.attr.isNightMode), textAttributes.isNightMode)
                )
            } finally {
                array.recycle()
            }
    }
}