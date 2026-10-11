package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.text.SpannedString
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseBenefitByMonthBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveResourceIdAttribute
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.text.DecimalFormat
import java.util.Currency
import java.util.TimeZone
import java.util.Locale

class CourseBenefitsMonthlyAdapterDelegate(
    private val revenuePriceMapper: RevenuePriceMapper
) : AdapterDelegate<CourseBenefitByMonthListItem, DelegateViewHolder<CourseBenefitByMonthListItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitByMonthListItem): Boolean =
        data is CourseBenefitByMonthListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitByMonthListItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefit_by_month))

    private inner class ViewHolder(
        view: android.view.View
    ) : DelegateViewHolder<CourseBenefitByMonthListItem>(view) {

        private val viewBinding: ItemCourseBenefitByMonthBinding by viewBinding(ItemCourseBenefitByMonthBinding::bind)

        override fun onBind(data: CourseBenefitByMonthListItem) {
            data as CourseBenefitByMonthListItem.Data

            val currency = Currency.getInstance(data.courseBenefitByMonth.currencyCode)
            val decimalFormat = DecimalFormat().apply { setCurrency(currency) }
            decimalFormat.minimumFractionDigits = 2

            viewBinding.courseBenefitByMonthCurrentMonth.text = DateTimeHelper.getPrintableDate(
                data.courseBenefitByMonth.date,
                DateTimeHelper.DISPLAY_MONTH_YEAR_NOMINAL_PATTERN,
                TimeZone.getDefault()
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            val (incomeString, incomeStringColor) = resolveIncomeString(data.courseBenefitByMonth.totalUserIncome, data.courseBenefitByMonth.currencyCode, decimalFormat)
            viewBinding.courseBenefitByMonthIncome.text = incomeString
            viewBinding.courseBenefitByMonthIncome.setTextColor(incomeStringColor)
            viewBinding.courseBenefitByMonthSalesValue.text = revenuePriceMapper.mapToDisplayPrice(data.courseBenefitByMonth.currencyCode, decimalFormat.format(data.courseBenefitByMonth.totalTurnover.toDoubleOrNull() ?: 0.0))
            viewBinding.courseBenefitByMonthRefundsValue.text = revenuePriceMapper.mapToDisplayPrice(data.courseBenefitByMonth.currencyCode, decimalFormat.format(data.courseBenefitByMonth.totalRefunds.toDoubleOrNull() ?: 0.0))
            viewBinding.courseBenefitByMonthCountPaymentsCountValue.text = data.courseBenefitByMonth.countPayments.toString()
            viewBinding.courseBenefitByMonthStepikPaymentsValue.text = data.courseBenefitByMonth.countNonZPayments.toString()
            viewBinding.courseBenefitByMonthZLinkPaymentsValue.text = data.courseBenefitByMonth.countZPayments.toString()
            viewBinding.courseBenefitByMonthInvoicePaymentsValue.text = data.courseBenefitByMonth.countInvoicePayments.toString()
        }

        private fun resolveIncomeString(totalUserIncome: String, currencyCode: String, decimalFormat: DecimalFormat): Pair<SpannedString, Int> {
            val totalUserIncomeFloat = totalUserIncome.toFloatOrNull() ?: 0f
            return when {
                totalUserIncomeFloat > 0f -> {
                    revenuePriceMapper.mapToDisplayPrice(currencyCode, decimalFormat.format(totalUserIncome.toDouble()), debitPrefixRequired = true) to
                            ContextCompat.getColor(context, R.color.color_overlay_green)
                }
                totalUserIncomeFloat < 0f -> {
                    revenuePriceMapper.mapToDisplayPrice(currencyCode, decimalFormat.format(totalUserIncome.toDouble())) to
                            ContextCompat.getColor(context, R.color.color_overlay_red)
                }
                else -> {
                    revenuePriceMapper.mapToDisplayPrice(currencyCode, decimalFormat.format(totalUserIncome.toDouble()), debitPrefixRequired = true) to
                            ContextCompat.getColor(context, context.resolveResourceIdAttribute(android.R.attr.textColorPrimary))
                }
            }
        }
    }
}
