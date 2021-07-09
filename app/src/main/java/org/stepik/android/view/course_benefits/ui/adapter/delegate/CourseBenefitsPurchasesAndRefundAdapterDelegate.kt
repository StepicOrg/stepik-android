package org.stepik.android.view.course_benefits.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import org.stepic.droid.R
import org.stepik.android.view.course_benefits.ui.CourseBenefitOperationItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseBenefitsPurchasesAndRefundAdapterDelegate : AdapterDelegate<CourseBenefitOperationItem, DelegateViewHolder<CourseBenefitOperationItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitOperationItem): Boolean =
        data is CourseBenefitOperationItem.PurchasesAndRefunds

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitOperationItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefits_purchases_and_refunds))

    private class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefitOperationItem>(containerView), LayoutContainer {
        override fun onBind(data: CourseBenefitOperationItem) {
            data as CourseBenefitOperationItem.PurchasesAndRefunds
        }
    }
}