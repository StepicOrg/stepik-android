package org.stepic.droid.adaptive.ui.custom.container

import android.view.ViewGroup


abstract class ContainerAdapter<VH : ContainerView.ViewHolder> {
    var container: ContainerView? = null

    protected fun onDataSetChanged() {
        container?.onDataSetChanged()
    }

    protected fun onDataAdded() {
        container?.onDataAdded()
    }

    protected fun onRebind() {
        container?.onRebind()
    }

    protected fun onRebind(pos: Int) {
        container?.onRebind(pos)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup) : VH
    abstract fun getItemCount() : Int
    abstract fun onBindViewHolder(holder: VH, pos: Int)
}