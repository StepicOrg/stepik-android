package org.stepik.android.view.base.ui.widget.attributes

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import org.stepic.droid.R

data class TextAttributes(
    val textSize: Int,
    @ColorInt
    val textColor: Int,
    @ColorInt
    val textColorHighlight: Int,
    val textIsSelectable: Boolean,

    @FontRes
    val fontResId: Int
) {
    companion object {
        @SuppressLint("ResourceType")
        fun fromAttributeSet(context: Context, attrs: AttributeSet?): TextAttributes {
            val set = intArrayOf(
                android.R.attr.textSize,
                android.R.attr.textColor,
                android.R.attr.textColorHighlight,
                android.R.attr.fontFamily,
                android.R.attr.textIsSelectable
            )

            val array = context.obtainStyledAttributes(attrs, set)
            try {
                return TextAttributes(
                    textSize = array.getDimensionPixelSize(0, 14),
                    textColor = array.getColor(1, 0xFF000000.toInt()),
                    textColorHighlight = array.getColor(2, ContextCompat.getColor(context, R.color.text_color_highlight)),
                    textIsSelectable = array.getBoolean(3, false),
                    fontResId = array.getResourceId(4, R.font.roboto_regular)
                )
            } finally {
                array.recycle()
            }
        }
    }
}