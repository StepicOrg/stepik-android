package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_benefit.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.text.DecimalFormat
import java.util.Currency
import java.util.TimeZone

class CourseBenefitsAdapterDelegate(
    private val revenuePriceMapper: RevenuePriceMapper,
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

            val currency = Currency.getInstance(data.courseBenefit.currencyCode)
            val decimalFormat = DecimalFormat().apply { setCurrency(currency) }
            decimalFormat.minimumFractionDigits = 2

            purchaseRefundIcon.setImageDrawable(getIconDrawable(data.courseBenefit))
            purchaseRefundName.text =
                if (data.courseBenefit.isManual) {
                    buildString {
                        append(context.getString(R.string.transaction_manual_channel))
                        if (data.courseBenefit.description != null) {
                            append(": ${data.courseBenefit.description}")
                        }
                    }
                } else {
                    data.user?.fullName ?: data.courseBenefit.buyer.toString()
                }

            purchaseRefundDate.text = DateTimeHelper.getPrintableDate(
                data.courseBenefit.time,
                DateTimeHelper.DISPLAY_DATETIME_PATTERN,
                TimeZone.getDefault()
            )

            val transactionSum = if (data.courseBenefit.status == CourseBenefit.Status.DEBITED) {
                revenuePriceMapper.mapToDisplayPrice(data.courseBenefit.currencyCode, decimalFormat.format(data.courseBenefit.paymentAmount?.toDoubleOrNull() ?: 0.0))
            } else {
                context.getString(R.string.course_benefits_refund)
            }

            val amount = revenuePriceMapper.mapToDisplayPrice(
                data.courseBenefit.currencyCode,
                decimalFormat.format(data.courseBenefit.amount.toDoubleOrNull() ?: 0.0),
                debitPrefixRequired = data.courseBenefit.status == CourseBenefit.Status.DEBITED
            )

            val textColor = if (data.courseBenefit.status == CourseBenefit.Status.DEBITED) {
                ContextCompat.getColor(context, R.color.material_on_background_emphasis_high_type)
            } else {
                ContextCompat.getColor(context, R.color.color_overlay_red)
            }
            purchaseRefundIncomeSum.setTextColor(textColor)
            purchaseRefundTransactionSum.text = transactionSum
            purchaseRefundIncomeSum.text = amount
            purchaseRefundPromocode.text = data.courseBenefit.promoCode
            purchaseRefundPromocode.isVisible = data.courseBenefit.promoCode != null
        }

        private fun getIconDrawable(data: CourseBenefit): Drawable? =
            if (data.isManual) {
                getTintedDrawable(R.color.color_on_surface_alpha_38)
            } else {
                if (data.status == CourseBenefit.Status.DEBITED) {
                    when {
                        data.isZLinkUsed == true ->
                            AppCompatResources.getDrawable(context, R.drawable.ic_purchase_z_link)

                        data.isInvoicePayment ->
                            getTintedDrawable(R.color.color_on_background)

                        else ->
                            AppCompatResources.getDrawable(context, R.drawable.ic_purchase_stepik)
                    }
                } else {
                    AppCompatResources.getDrawable(context, R.drawable.ic_refund)
                }
            }

        private fun getTintedDrawable(tint: Int): Drawable? =
            AppCompatResources
                .getDrawable(context, R.drawable.ic_purchase_stepik)
                ?.mutate()
                ?.let { DrawableCompat.wrap(it) }
                ?.also {
                    DrawableCompat.setTint(it, ContextCompat.getColor(context, tint))
                    DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
                }
    }
}