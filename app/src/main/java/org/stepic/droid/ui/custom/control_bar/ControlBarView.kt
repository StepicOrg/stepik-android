package org.stepic.droid.ui.custom.control_bar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import org.stepic.droid.R
import org.stepic.droid.util.resolveAttribute
import kotlin.math.max

class ControlBarView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private val inflater = LayoutInflater.from(context)
    private val menu: Menu =
            PopupMenu(context, this).menu

    @LayoutRes
    private val itemLayoutRes: Int

    private lateinit var actionMore: View
    private lateinit var popupMenu: PopupMenu

    private val invisibleItemIds = mutableSetOf<Int>()

    var onClickListener: ((Int) -> Boolean)? = null

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

        invalidateMenu()
    }

    private fun invalidateMenu() {
        removeAllViews()
        initActionMore()
        initChildren()
    }

    private fun initChildren() {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)

            if (item.itemId in invisibleItemIds) continue // skip invisible item

            val view = inflater.inflate(itemLayoutRes, this, false)

            view.findViewById<TextView>(android.R.id.text1).text = item.title

            with(view.findViewById<ImageView>(android.R.id.icon)) {
                setImageDrawable(item.icon)
                visibility = if (item.icon != null) VISIBLE else GONE
            }

            view.id = item.itemId
            view.isEnabled = item.isEnabled
            view.visibility = if (item.isVisible) VISIBLE else GONE
            view.setOnClickListener(this)
            addView(view)
        }
    }

    private fun initActionMore() {
        actionMore = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setImageResource(R.drawable.ic_action_more_vert)
            ImageViewCompat
                .setImageTintList(this, ContextCompat.getColorStateList(context, com.google.android.material.R.color.mtrl_text_btn_text_color_selector))

            setBackgroundResource(context.resolveAttribute(R.attr.selectableItemBackgroundRounded)?.resourceId ?: 0)
            setPadding(16, 16, 16, 16)
        }
        addView(actionMore)

        popupMenu = PopupMenu(context, actionMore)
        popupMenu.setOnMenuItemClickListener(this)

        actionMore.setOnClickListener { popupMenu.show() }
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
            if (child.visibility == GONE) continue
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
        popupMenu.menu.clear()

        var x = left + paddingLeft
        val xMax = right - paddingRight - actionMore.measuredWidth
        val yCenter = (bottom - top) / 2
        var i = 0

        while (i in 0 until childCount) { // layout view while there is room
            val child = getChildAt(i++)

            if (child == actionMore || child.visibility == GONE) continue
            if (x + child.measuredWidth > xMax) {
                i--
                break
            }

            layoutChildInCenter(child, x, yCenter)
            x += child.measuredWidth
        }

        if (i < childCount) { // show more action
            layoutChildInCenter(actionMore, xMax, yCenter)
        }

        while (i in 0 until childCount) { // fill more action popup
            val child = getChildAt(i++)

            if (child == actionMore || child.visibility == GONE) continue

            val title = child.findViewById<TextView>(android.R.id.text1).text
            popupMenu.menu.add(0, child.id, Menu.NONE, title)
        }
    }

    private fun layoutChildInCenter(child: View, x: Int, yCenter: Int) {
        val top = yCenter - child.measuredHeight / 2
        child.layout(x, top, x + child.measuredWidth, top + child.measuredHeight)
    }

    override fun onClick(view: View) {
        onClickListener?.invoke(view.id)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean =
        onClickListener
            ?.invoke(item.itemId)
            ?: false

    fun changeItemVisibility(@IdRes id: Int, isVisible: Boolean) {
        if (isVisible) {
            invisibleItemIds -= id
        } else {
            invisibleItemIds += id
        }
        invalidateMenu()
    }
}