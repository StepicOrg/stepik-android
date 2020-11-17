package org.stepik.android.view.course_list.ui.adapter.decorator

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.ui.util.inflate

class CourseListCollectionHeaderDecoration : RecyclerView.ItemDecoration() {
    var collectionDescriptionColors: CollectionDescriptionColors? = null
        set(value) {
            field = value
            header = null
        }

    var headerText: String? = null
        set(value) {
            field = value
            header = null
        }

    private var header: PlaceholderTextView? = null

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val collectionDescriptionColors =
            this.collectionDescriptionColors ?: return

        initHeader(parent, collectionDescriptionColors)
        val header = this.header ?: return

        val child = parent.getChildAt(0)
        canvas.save()

        val left = child.left - parent.context.resources.getDimensionPixelOffset(R.dimen.padding_placeholders)
        val top = child.y.toInt() - header.height - parent.context.resources.getDimensionPixelOffset(R.dimen.padding_placeholders)
        canvas.translate(left.toFloat(), top.toFloat())

        header.translationX = left.toFloat()
        header.translationX = top.toFloat()
        header.draw(canvas)
        canvas.restore()
    }

    private fun initHeader(parent: RecyclerView, collectionDescriptionColors: CollectionDescriptionColors) {
        if (header == null) {
            val view = parent.inflate(R.layout.item_course_collection_header) as PlaceholderTextView

            view.setPlaceholderText(headerText.orEmpty())
            view.setBackgroundResource(collectionDescriptionColors.backgroundResSquared)
            view.setTextColor(AppCompatResources.getColorStateList(parent.context, collectionDescriptionColors.textColorRes))

            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

            val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                0, view.layoutParams.width)
            val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom, view.layoutParams.height)

            view.measure(childWidth, childHeight)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            header = view
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val collectionDescriptionColors =
            this.collectionDescriptionColors ?: return

        val columnsCount = (parent.layoutManager as? GridLayoutManager)
            ?.spanCount
            ?: 1

        (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it < columnsCount }
            ?: return

        initHeader(parent, collectionDescriptionColors)
        val header = this.header ?: return

        outRect.set(0, header.height, 0, 0)
    }
}