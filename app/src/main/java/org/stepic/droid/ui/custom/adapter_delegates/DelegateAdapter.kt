package org.stepic.droid.ui.custom.adapter_delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

abstract class DelegateAdapter<D, VH : DelegateViewHolder<D>> : RecyclerView.Adapter<VH>() {
    private val _delegates = mutableListOf<AdapterDelegate<D, VH>>()

    val delegates: List<AdapterDelegate<D, VH>>
        get() = _delegates

    fun addDelegate(delegate: AdapterDelegate<D, VH>) =
        _delegates.add(delegate)

    fun removeDelegate(delegate: AdapterDelegate<D, VH>) =
        _delegates.remove(delegate)

    override fun getItemViewType(position: Int): Int =
        delegates.indexOfFirst { it.isForViewType(position, getItemAtPosition(position)) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        delegates[viewType].onCreateViewHolder(parent)

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItemAtPosition(position))

    abstract fun getItemAtPosition(position: Int): D
}