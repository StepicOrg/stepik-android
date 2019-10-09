package org.stepic.droid.ui.adapters.viewhoders

import androidx.recyclerview.widget.RecyclerView
import android.view.View

abstract class GenericViewHolder<T>(root: View): RecyclerView.ViewHolder(root) {
    abstract fun onBind(item: T)
}