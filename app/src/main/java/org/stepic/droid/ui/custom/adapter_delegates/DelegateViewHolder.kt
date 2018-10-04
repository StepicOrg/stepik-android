package org.stepic.droid.ui.custom.adapter_delegates

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class DelegateViewHolder<D>(root: View) : RecyclerView.ViewHolder(root) {
    protected var itemData: D? = null
        private set

    internal fun bind(data: D) {
        itemData = data
        onBind(data)
    }

    protected open fun onBind(data: D) {}
}