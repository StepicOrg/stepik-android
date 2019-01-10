package org.stepic.droid.ui.util

import android.support.v7.widget.*
import android.util.DisplayMetrics
import android.view.View

class CoursesSnapHelper(private val rowCount: Int) : SnapHelper() {

    companion object {
        private const val MAX_DURATION_OF_SCROLL = 100
        private const val MILLISECONDS_PER_INCH = 100f
    }

    private var horizontalHelper: OrientationHelper? = null
    private var recyclerView: RecyclerView? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        out[1] = 0
        return out
    }


    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int =
            helper.getDecoratedStart(targetView) - helper.startAfterPadding

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (horizontalHelper == null) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return horizontalHelper!!
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? =
            getStartView(layoutManager, getHorizontalHelper(layoutManager), true)

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val forwardDirection = velocityX > 0
        val startMostChildView = getStartView(layoutManager, getHorizontalHelper(layoutManager), forwardDirection)

        return if (startMostChildView == null) {
            RecyclerView.NO_POSITION
        } else {
            layoutManager.getPosition(startMostChildView)
        }
    }

    private fun getStartView(
            layoutManager: RecyclerView.LayoutManager,
            helper: OrientationHelper,
            forwardDirection: Boolean): View? {

        val gridLayoutManager = layoutManager as LinearLayoutManager
        val firstChildPosition = gridLayoutManager.findFirstVisibleItemPosition()

        val isLastItem = gridLayoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1

        if (firstChildPosition == RecyclerView.NO_POSITION || isLastItem) {
            return null
        }

        val child = layoutManager.findViewByPosition(firstChildPosition)
        val endOfChild = helper.getDecoratedEnd(child)
        val threshold = helper.getDecoratedMeasurement(child)
        return if (endOfChild >= threshold && endOfChild > 0) {
            child
        } else if (forwardDirection) {
            getNextView(firstChildPosition, layoutManager)
        } else {
            getPreviousView(firstChildPosition, layoutManager)
        }
    }

    private fun getNextView(currentPosition: Int, layoutManager: RecyclerView.LayoutManager): View? {
        val lastPosition = layoutManager.itemCount - 1
        val currentPositionPlusRowCount = currentPosition + rowCount
        val nextPosition = Math.min(lastPosition, currentPositionPlusRowCount)
        return layoutManager.findViewByPosition(nextPosition)
    }

    private fun getPreviousView(currentPosition: Int, layoutManager: RecyclerView.LayoutManager): View? {
        val previousPosition = Math.max(0, currentPosition)
        return layoutManager.findViewByPosition(previousPosition)
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): RecyclerView.SmoothScroller? {
        return object : LinearSmoothScroller(recyclerView?.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
                    MILLISECONDS_PER_INCH / displayMetrics.densityDpi

            override fun calculateTimeForScrolling(dx: Int): Int =
                    Math.min(MAX_DURATION_OF_SCROLL, super.calculateTimeForScrolling(dx))
        }
    }

}
