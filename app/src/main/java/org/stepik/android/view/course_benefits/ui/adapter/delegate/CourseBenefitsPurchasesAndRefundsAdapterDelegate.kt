package org.stepik.android.view.course_benefits.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_purchase_refund.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_benefits.model.CourseBenefit
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.TimeZone

class CourseBenefitsPurchasesAndRefundsAdapterDelegate(
    private val displayPriceMapper: DisplayPriceMapper
) : AdapterDelegate<CourseBenefit, DelegateViewHolder<CourseBenefit>>() {
    override fun isForViewType(position: Int, data: CourseBenefit): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefit> =
        ViewHolder(createView(parent, R.layout.item_purchase_refund))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefit>(containerView), LayoutContainer {
        override fun onBind(data: CourseBenefit) {
            purchaseRefundIcon.setImageResource(getIconRes(data))
            purchaseRefundName.text = data.buyer.toString()
            purchaseRefundDate.text = DateTimeHelper.getPrintableDate(
                data.time,
                DateTimeHelper.DISPLAY_DATETIME_PATTERN,
                TimeZone.getDefault()
            )

            val amount = displayPriceMapper.mapToDisplayPrice(data.currencyCode, data.amount)
            val resolvedAmount = if (data.status == CourseBenefit.Status.DEBITED) {
                context.getString(R.string.course_benefits_debited, amount)
            } else {
                context.getString(R.string.course_benefits_refunded, amount)
            }

            val textColor = if (data.status == CourseBenefit.Status.DEBITED) {
                ContextCompat.getColor(context, R.color.material_on_background_emphasis_high_type)
            } else {
                ContextCompat.getColor(context, R.color.color_overlay_red_alpha_12)
            }
            purchaseRefundIncomeSum.setTextColor(textColor)
            purchaseRefundTransactionSum.text = displayPriceMapper.mapToDisplayPrice(data.currencyCode, data.paymentAmount)
            purchaseRefundIncomeSum.text = resolvedAmount
            purchaseRefundPromocode.text = data.promoCode
            purchaseRefundPromocode.isVisible = data.promoCode != null
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