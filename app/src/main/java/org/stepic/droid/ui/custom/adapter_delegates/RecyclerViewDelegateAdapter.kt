package org.stepic.droid.ui.custom.adapter_delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

abstract class RecyclerViewDelegateAdapter<D, VH : DelegateViewHolder<D>> : RecyclerView.Adapter<VH>() {
    private val delegates = mutableListOf<AdapterDelegate<D, VH>>()

    fun addDelegate(delegate: AdapterDelegate<D, VH>) =
            delegates.add(delegate)

    fun removeDelegate(delegate: AdapterDelegate<D, VH>) =
            delegates.remove(delegate)

    abstract fun getItemAtPosition(position: Int): D

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH =
            delegates.first { it.isForViewType(position) }.onCreateViewHolder(parent)

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItemAtPosition(position))
}