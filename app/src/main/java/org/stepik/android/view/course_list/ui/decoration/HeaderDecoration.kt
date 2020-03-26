package org.stepik.android.view.course_list.ui.decoration

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.util.ColorUtil

class HeaderDecoration(private val headerText: String) : RecyclerView.ItemDecoration() {
    private var header: Bitmap? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val view = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                initHeader(parent)
                header?.let {
                    c.drawBitmap(
                        it,
                        0.toFloat(),
                        (view.top - it.height - parent.context.resources.getDimensionPixelOffset(R.dimen.padding_placeholders)).toFloat(),
                        Paint()
                    )
                    c.translate(view.left.toFloat(), parent.scrollY.toFloat())
                }
            }
        }
    }

    private fun initHeader(parent: RecyclerView) {
        if (header == null) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.course_collection_header_view, parent, false) as PlaceholderTextView

            view.setPlaceholderText(headerText)
            view.setBackgroundResource(CollectionDescriptionColors.FIRE.backgroundResSquared)
            view.setTextColor(ColorUtil.getColorArgb(CollectionDescriptionColors.FIRE.textColorRes, parent.context))

            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

            val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.paddingLeft + parent.paddingRight, view.layoutParams.width)
            val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom, view.layoutParams.height)

            view.measure(childWidth, childHeight)
            view.layout(0, 0, parent.width, view.measuredHeight)

            val bitmap = Bitmap.createBitmap(parent.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            view.draw(canvas)
            header = bitmap
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it == 0 }
            ?: return

        outRect.set(0, view.height - parent.context.resources.getDimensionPixelOffset(R.dimen.padding_placeholders), 0, 0)
    }
}