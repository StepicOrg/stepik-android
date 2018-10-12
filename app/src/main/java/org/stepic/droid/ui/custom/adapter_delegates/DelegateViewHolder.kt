package org.stepic.droid.ui.custom.adapter_delegates

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View

abstract class DelegateViewHolder<D>(root: View) : RecyclerView.ViewHolder(root) {
    protected var itemData: D? = null
        private set

    protected val context: Context
        get() = itemView.context

    internal fun bind(data: D) {
        itemData = data
        onBind(data)
    }

    protected open fun onBind(data: D) {}
}