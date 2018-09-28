package org.stepic.droid.ui.custom

import android.graphics.Canvas
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import java.util.HashMap

/**
 * A sticky header decoration for android's RecyclerView.
 */
class StickyHeaderDecoration <VH : RecyclerView.ViewHolder>
/**
 * @param mAdapter
 * the sticky header adapter to use
 */
@JvmOverloads constructor(private val mAdapter: StickyHeaderAdapter<VH>, private val mRenderInline: Boolean = false) : RecyclerView.ItemDecoration() {
    private val mHeaderCache: MutableMap<Long, VH> = HashMap()


    /**
     * {@inheritDoc}
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        var headerHeight = 0

        if (position != RecyclerView.NO_POSITION
                && hasHeader(position)
                && showHeaderAboveItem(position)) {

            val header = getHeader(parent, position).itemView
            headerHeight = getHeaderHeightForLayout(header)
        }

        outRect.set(0, headerHeight, 0, 0)
    }

    private fun showHeaderAboveItem(itemAdapterPosition: Int): Boolean {
        return if (itemAdapterPosition == 0) {
            true
        } else mAdapter.getHeaderId(itemAdapterPosition - 1) != mAdapter.getHeaderId(itemAdapterPosition)
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    fun clearHeaderCache() {
        mHeaderCache.clear()
    }

    fun findHeaderViewUnder(x: Float, y: Float): View? {
        for (holder in mHeaderCache.values) {
            val child = holder.itemView
            val translationX = child.translationX
            val translationY = child.translationY

            if (x >= child.left + translationX &&
                    x <= child.right + translationX &&
                    y >= child.top + translationY &&
                    y <= child.bottom + translationY) {
                return child
            }
        }

        return null
    }

    private fun hasHeader(position: Int) =
            mAdapter.getHeaderId(position) != NO_HEADER_ID

    private fun getHeader(parent: RecyclerView, position: Int): RecyclerView.ViewHolder {
        val key = mAdapter.getHeaderId(position)

        if (mHeaderCache.containsKey(key)) {
            return mHeaderCache[key]!!
        } else {
            val holder = mAdapter.onCreateHeaderViewHolder(parent)
            val header = holder.itemView


            mAdapter.onBindHeaderViewHolder(holder, position)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

            val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.paddingLeft + parent.paddingRight, header.layoutParams.width)
            val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.paddingTop + parent.paddingBottom, header.layoutParams.height)

            header.measure(childWidth, childHeight)
            header.layout(0, 0, header.measuredWidth, header.measuredHeight)

            mHeaderCache.put(key, holder)

            return holder
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val count = parent.childCount
        var previousHeaderId: Long = -1

        for (layoutPos in 0 until count) {
            val child = parent.getChildAt(layoutPos)
            val adapterPos = parent.getChildAdapterPosition(child)

            if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
                val headerId = mAdapter.getHeaderId(adapterPos)

                if (headerId != previousHeaderId) {
                    previousHeaderId = headerId
                    val header = getHeader(parent, adapterPos).itemView
                    canvas.save()

                    val left = child.left
                    val top = getHeaderTop(parent, child, header, adapterPos, layoutPos)
                    canvas.translate(left.toFloat(), top.toFloat())

                    header.translationX = left.toFloat()
                    header.translationY = top.toFloat()
                    header.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun getHeaderTop(parent: RecyclerView, child: View, header: View, adapterPos: Int, layoutPos: Int): Int {
        val headerHeight = getHeaderHeightForLayout(header)
        var top = child.y.toInt() - headerHeight
        if (layoutPos == 0) {
            val count = parent.childCount
            val currentId = mAdapter.getHeaderId(adapterPos)
            // find next view with header and compute the offscreen push if needed
            for (i in 1 until count) {
                val adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(i))
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    val nextId = mAdapter.getHeaderId(adapterPosHere)
                    if (nextId != currentId) {
                        val next = parent.getChildAt(i)
                        val offset = next.y.toInt() - (headerHeight + getHeader(parent, adapterPosHere).itemView.height)
                        if (offset < 0) {
                            return offset
                        } else {
                            break
                        }
                    }
                }
            }

            top = Math.max(0, top)
        }

        return top
    }

    private fun getHeaderHeightForLayout(header: View) =
            if (mRenderInline) 0 else header.height

    companion object {
        const val NO_HEADER_ID = -1L
    }
}