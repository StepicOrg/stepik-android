package org.stepic.droid.ui.custom.adapter_delegates

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class AdapterDelegate<D, VH : DelegateViewHolder<D>>(
        private val adapter: DelegateAdapter<D, VH>
) {
    protected fun getItemAtPosition(position: Int): D =
            adapter.getItemAtPosition(position)

    protected fun createView(parent: ViewGroup, @LayoutRes layoutId: Int): View =
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun isForViewType(position: Int): Boolean
}