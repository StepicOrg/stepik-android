package org.stepic.droid.ui.adapters.viewhoders

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class GenericViewHolder<T>(root: View): RecyclerView.ViewHolder(root) {
    abstract fun onBind(item: T)
}