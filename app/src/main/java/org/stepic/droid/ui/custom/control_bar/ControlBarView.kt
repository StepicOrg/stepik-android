package org.stepic.droid.ui.custom.control_bar

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import org.stepic.droid.R

class ControlBarView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
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
            view.findViewById<ImageView>(android.R.id.icon).setImageDrawable(item.icon)

            addView(view)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        
    }
}