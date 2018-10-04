package org.stepic.droid.ui.custom.adapter_delegates

import android.view.ViewGroup

abstract class AdapterDelegate<D, VH : DelegateViewHolder<D>>(
        private val adapter: RecyclerViewDelegateAdapter<D, VH>
) {
    protected fun getItemAtPosition(position: Int): D =
            adapter.getItemAtPosition(position)

    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun isForViewType(position: Int): Boolean
}