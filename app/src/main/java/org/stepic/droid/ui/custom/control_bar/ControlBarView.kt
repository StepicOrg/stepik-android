package org.stepic.droid.ui.custom.control_bar

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import org.stepic.droid.R
import kotlin.math.max

class ControlBarView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val inflater = LayoutInflater.from(context)
    private val menu: Menu =
            PopupMenu(context, null).menu

    @LayoutRes
    private val itemLayoutRes: Int

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ControlBarView)

        try {
            val menuInflater = MenuInflater(context)
            @MenuRes
            val menuRes = typedArray.getResourceId(R.styleable.ControlBarView_menu, 0)

            if (menuRes != 0) {
                menuInflater.inflate(menuRes, menu)
            }

            itemLayoutRes = typedArray.getResourceId(R.styleable.ControlBarView_item_layout, 0)
        } finally {
            typedArray.recycle()
        }

        initChildren()
    }

    private fun initChildren() {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val view = inflater.inflate(itemLayoutRes, this, false)

            view.findViewById<TextView>(android.R.id.text1).text = item.title

            with(view.findViewById<ImageView>(android.R.id.icon)) {
                setImageDrawable(item.icon)
                visibility = if (item.icon != null) VISIBLE else GONE
            }

            view.id = item.itemId
            view.setOnClickListener(this)
            addView(view)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        var height = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            width += child.measuredWidth
            height = max(height, child.measuredHeight)
        }

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var x = left + paddingLeft
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            val t = (bottom - top - child.measuredHeight) / 2
            child.layout(x, t, x + child.measuredWidth, t + child.measuredHeight)

            x += child.measuredWidth
        }
    }

    override fun onClick(view: View) {
        Log.d(javaClass.canonicalName, "on view click $view")
    }
}