package org.stepic.droid.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import org.stepic.droid.adaptive.ui.custom.container.ContainerAdapter
import org.stepic.droid.adaptive.ui.custom.container.ContainerView

class ListContainer
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), ContainerView {
    init {
        orientation = VERTICAL
    }

    private val holders = ArrayList<ContainerView.ViewHolder>()
    private var adapter: ContainerAdapter<ContainerView.ViewHolder>? = null

    override fun setAdapter(adapter: ContainerAdapter<out ContainerView.ViewHolder>) {
        this.adapter = adapter as ContainerAdapter<ContainerView.ViewHolder>
        this.adapter?.container = this
        holders.clear()
        for (i in 0 until adapter.getItemCount())
            holders.add(adapter.onCreateViewHolder(this))
        onDataSetChanged()
    }

    fun getAdapter() = adapter

    override fun onDataSetChanged() {
        removeAllViews()
        onRebind()
    }

    override fun onDataAdded() = onRebind()

    override fun onRebind() {
        Log.d(javaClass.canonicalName, "size: " + holders.size)
        for (i in holders.indices)
            onRebind(i)
    }

    override fun onRebind(pos: Int) {
        Log.d(javaClass.canonicalName, "onAttachHolder")
        adapter?.let {
            it.onBindViewHolder(holders[pos], pos)
            if (!holders[pos].isAttached) {
                holders[pos].isAttached = true
                addView(holders[pos].view, pos)
            }
        }
    }
}