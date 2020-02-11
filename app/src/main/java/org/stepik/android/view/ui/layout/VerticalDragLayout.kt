package org.stepik.android.view.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * Allow listen vertical drag motions.
 */
internal class VerticalDragLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var draggingIsEnabled = true
        set(value) {
            field = value
            reset()
        }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // null - not detect move, true - vertical, false - horizontal
    private var isDetectedVerticalMove: Boolean? = null

    private var startX = 0f
    private var startY = 0f

    private var startInnerMoveY = 0f

    private var onDragListener: (dy: Float) -> Unit = {}
    private var onReleaseDragListener: (dy: Float) -> Unit = {}

    fun setOnDragListener(listener: (dy: Float) -> Unit) {
        onDragListener = listener
    }

    fun setOnReleaseDragListener(listener: (dy: Float) -> Unit) {
        onReleaseDragListener = listener
    }

    private fun reset() {
        isDetectedVerticalMove = null
        startX = 0f
        startY = 0f
        startInnerMoveY = 0f
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (ev.pointerCount == 1) {
                    startX = ev.x
                    startY = ev.y
                } else {
                    isDetectedVerticalMove = null
                    startX = 0f
                    startY = 0f
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isDetectedVerticalMove = null
                startX = 0f
                startY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggingIsEnabled &&
                    isDetectedVerticalMove == null &&
                    ev.pointerCount == 1 &&
                    abs(startY - ev.y) > touchSlop
                ) {
                    val direction = getDirection(ev, startX, startY)
                    isDetectedVerticalMove = (direction == Direction.UP || direction == Direction.DOWN)
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        ev.pointerCount == 1 &&
                ev.action == MotionEvent.ACTION_MOVE &&
                isDetectedVerticalMove == true

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (ev.pointerCount == 1) {
                    onReleaseDragListener(startInnerMoveY - ev.y)
                }
                startInnerMoveY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                if (startInnerMoveY == 0f) {
                    startInnerMoveY = ev.y
                }
                onDragListener(startInnerMoveY - ev.y)
            }
        }
        return true
    }

    private fun getDirection(ev: MotionEvent, x: Float, y: Float) =
        Direction[getAngle(x, y, ev.x, ev.y)]

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val rad = Math.atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    private enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        companion object {

            private const val V_ANGLE = 30F

            operator fun get(angle: Double): Direction =
                when {
                    inRange(angle, 90f - V_ANGLE, 90f + V_ANGLE) -> UP
                    inRange(angle, 0f, 90f - V_ANGLE) || inRange(angle, 270f + V_ANGLE, 360f) -> RIGHT
                    inRange(angle, 270f - V_ANGLE, 270f + V_ANGLE) -> DOWN
                    else -> LEFT
                }

            private fun inRange(angle: Double, init: Float, end: Float) =
                angle >= init && angle < end
        }
    }
}