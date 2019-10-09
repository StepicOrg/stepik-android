package org.stepic.droid.ui.adapters.viewhoders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.loading_view.view.*
import org.stepic.droid.ui.util.changeVisibility
import timber.log.Timber

class FooterItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val loadingRoot: View = view.loadingRoot

    fun setLoaderVisibiluty(isNeedShow: Boolean) {
        loadingRoot.changeVisibility(isNeedShow)
    }
}
