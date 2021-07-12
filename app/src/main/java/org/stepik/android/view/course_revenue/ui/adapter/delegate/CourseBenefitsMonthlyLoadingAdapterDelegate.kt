package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseBenefitsMonthlyLoadingAdapterDelegate : AdapterDelegate<CourseBenefitByMonthListItem, DelegateViewHolder<CourseBenefitByMonthListItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitByMonthListItem): Boolean =
        data is CourseBenefitByMonthListItem.Placeholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitByMonthListItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefit_by_month_skeleton))

    private class ViewHolder(root: View) : DelegateViewHolder<CourseBenefitByMonthListItem>(root)
}