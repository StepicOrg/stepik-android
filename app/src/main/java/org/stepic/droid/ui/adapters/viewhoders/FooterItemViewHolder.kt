package org.stepic.droid.ui.adapters.viewhoders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.loading_view.view.*

class FooterItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val loadingRoot: View = view.loadingRoot

    fun setLoaderVisibiluty(isNeedShow: Boolean) {
        loadingRoot.isVisible = isNeedShow
    }
}
