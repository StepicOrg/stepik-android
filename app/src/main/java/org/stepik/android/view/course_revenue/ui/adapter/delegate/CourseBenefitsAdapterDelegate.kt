package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_benefit.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.TimeZone

class CourseBenefitsAdapterDelegate(
    private val displayPriceMapper: DisplayPriceMapper,
    private val onItemClick: (CourseBenefitListItem.Data) -> Unit
) : AdapterDelegate<CourseBenefitListItem, DelegateViewHolder<CourseBenefitListItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitListItem): Boolean =
        data is CourseBenefitListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitListItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefit))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefitListItem>(containerView), LayoutContainer {

        init {
            itemView.setOnClickListener { (itemData as? CourseBenefitListItem.Data)?.let { onItemClick(it) } }
        }

        override fun onBind(data: CourseBenefitListItem) {
            data as CourseBenefitListItem.Data
            purchaseRefundIcon.setImageResource(getIconRes(data.courseBenefit))
            purchaseRefundName.text = data.user?.fullName ?: data.courseBenefit.buyer.toString()
            purchaseRefundDate.text = DateTimeHelper.getPrintableDate(
                data.courseBenefit.time,
                DateTimeHelper.DISPLAY_DATETIME_PATTERN,
                TimeZone.getDefault()
            )

            val transactionSum = if (data.courseBenefit.status == CourseBenefit.Status.DEBITED) {
                displayPriceMapper.mapToDisplayPrice(data.courseBenefit.currencyCode, data.courseBenefit.paymentAmount)
            } else {
                context.getString(R.string.course_benefits_refund)
            }

            val amount = displayPriceMapper.mapToDisplayPrice(data.courseBenefit.currencyCode, data.courseBenefit.amount)
            val resolvedAmount = if (data.courseBenefit.status == CourseBenefit.Status.DEBITED) {
                context.getString(R.string.course_benefits_with_debited_prefix, amount)
            } else {
                amount
            }

            val textColor = if (data.courseBenefit.status == CourseBenefit.Status.DEBITED) {
                ContextCompat.getColor(context, R.color.material_on_background_emphasis_high_type)
            } else {
                ContextCompat.getColor(context, R.color.color_overlay_red)
            }
            purchaseRefundIncomeSum.setTextColor(textColor)
            purchaseRefundTransactionSum.text = transactionSum
            purchaseRefundIncomeSum.text = resolvedAmount
            purchaseRefundPromocode.text = data.courseBenefit.promoCode
            purchaseRefundPromocode.isVisible = data.courseBenefit.promoCode != null
        }

        private fun getIconRes(data: CourseBenefit): Int =
            if (data.status == CourseBenefit.Status.DEBITED) {
                if (data.isZLinkUsed) {
                    R.drawable.ic_purchase_z_link
                } else {
                    R.drawable.ic_purchase_stepik
                }
            } else {
                R.drawable.ic_refund
            }
    }
}