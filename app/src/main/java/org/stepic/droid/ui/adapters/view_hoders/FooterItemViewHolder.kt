package org.stepic.droid.ui.adapters.view_hoders

import android.view.View
import kotlinx.android.synthetic.main.loading_view.view.*

class FooterItemViewHolder(view: View, private val isNeedShowFooter: Boolean?) : CourseViewHolderBase(view) {
    override fun setDataOnView(position: Int) {
        itemView.loadingRoot.visibility = if (isNeedShowFooter ?: false) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}