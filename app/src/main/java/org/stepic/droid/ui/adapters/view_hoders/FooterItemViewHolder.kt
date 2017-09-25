package org.stepic.droid.ui.adapters.view_hoders

import android.view.View
import kotlinx.android.synthetic.main.loading_view.view.*

class FooterItemViewHolder(view: View, private val state: State) : CourseViewHolderBase(view) {
    override fun setDataOnView(position: Int) {
        itemView.loadingRoot.visibility = if (state.isNeedShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {
        data class State(var isNeedShow: Boolean)
    }
}
