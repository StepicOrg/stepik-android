package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseBenefitsLoadingAdapterDelegate : AdapterDelegate<CourseBenefitListItem, DelegateViewHolder<CourseBenefitListItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitListItem): Boolean =
        data is CourseBenefitListItem.Placeholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitListItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefit_skeleton))

    private class ViewHolder(root: View) : DelegateViewHolder<CourseBenefitListItem>(root)
}