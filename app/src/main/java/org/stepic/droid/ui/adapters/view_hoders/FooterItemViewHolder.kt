package org.stepic.droid.ui.adapters.view_hoders

import android.view.View
import kotlinx.android.synthetic.main.loading_view.view.*
import timber.log.Timber

class FooterItemViewHolder(view: View, private val state: State) : CourseViewHolderBase(view) {

    private val loadingRoot: View = view.loadingRoot

    override fun setDataOnView(position: Int) {
        Timber.d("loadMore loadView.state = ${state.isNeedShow}")
        loadingRoot.visibility = if (state.isNeedShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        data class State(var isNeedShow: Boolean)
    }
}
