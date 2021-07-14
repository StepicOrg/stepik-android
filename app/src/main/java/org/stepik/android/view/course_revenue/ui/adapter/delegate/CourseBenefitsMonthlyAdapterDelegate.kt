package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_benefit_by_month.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveResourceIdAttribute
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.Locale
import java.util.TimeZone

class CourseBenefitsMonthlyAdapterDelegate(
    private val displayPriceMapper: DisplayPriceMapper
) : AdapterDelegate<CourseBenefitByMonthListItem, DelegateViewHolder<CourseBenefitByMonthListItem>>() {
    override fun isForViewType(position: Int, data: CourseBenefitByMonthListItem): Boolean =
        data is CourseBenefitByMonthListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitByMonthListItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefit_by_month))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefitByMonthListItem>(containerView), LayoutContainer {

        override fun onBind(data: CourseBenefitByMonthListItem) {
            data as CourseBenefitByMonthListItem.Data

            courseBenefitByMonthCurrentMonth.text = DateTimeHelper.getPrintableDate(
                data.courseBenefitByMonth.date,
                DateTimeHelper.DISPLAY_MONTH_YEAR_NOMINAL_PATTERN,
                TimeZone.getDefault()
            ).capitalize(Locale.ROOT)

            val (incomeString, incomeStringColor) = resolveIncomeString(data.courseBenefitByMonth.totalUserIncome, data.courseBenefitByMonth.currencyCode)
            courseBenefitByMonthIncome.text = incomeString
            courseBenefitByMonthIncome.setTextColor(incomeStringColor)
            courseBenefitByMonthSalesValue.text = displayPriceMapper.mapToDisplayPrice(data.courseBenefitByMonth.currencyCode, data.courseBenefitByMonth.totalTurnover)
            courseBenefitByMonthRefundsValue.text = displayPriceMapper.mapToDisplayPrice(data.courseBenefitByMonth.currencyCode, data.courseBenefitByMonth.totalRefunds)
            courseBenefitByMonthCountPaymentsCountValue.text = data.courseBenefitByMonth.countPayments.toString()
            courseBenefitByMonthStepikPaymentsValue.text = data.courseBenefitByMonth.countNonZPayments.toString()
            courseBenefitByMonthZLinkPaymentsValue.text = data.courseBenefitByMonth.countZPayments.toString()
            courseBenefitByMonthInvoicePaymentsValue.text = data.courseBenefitByMonth.countInvoicePayments.toString()
        }

        private fun resolveIncomeString(totalUserIncome: String, currencyCode: String): Pair<String, Int> {
            val totalUserIncomeFloat = totalUserIncome.toFloatOrNull() ?: 0f
            return when {
                totalUserIncomeFloat > 0f -> {
                    context.getString(R.string.course_benefits_with_debited_prefix, displayPriceMapper.mapToDisplayPrice(currencyCode, totalUserIncome)) to
                            ContextCompat.getColor(context, R.color.color_overlay_green)
                }
                totalUserIncomeFloat < 0f -> {
                    displayPriceMapper.mapToDisplayPrice(currencyCode, totalUserIncome) to
                            ContextCompat.getColor(context, R.color.color_overlay_red)
                }
                else -> {
                    context.getString(R.string.course_benefits_with_debited_prefix, displayPriceMapper.mapToDisplayPrice(currencyCode, totalUserIncome)) to
                            ContextCompat.getColor(context, context.resolveResourceIdAttribute(android.R.attr.textColorPrimary))
                }
            }
        }
    }
}