package org.stepik.android.view.course_revenue.ui.delegate

import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseBenefitSummaryBinding
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import java.text.DecimalFormat
import java.util.Currency
import java.util.TimeZone
import java.util.Locale

class CourseBenefitSummaryViewDelegate(
    private val binding: ViewCourseBenefitSummaryBinding,
    private val revenuePriceMapper: RevenuePriceMapper,
    private val onCourseSummaryClicked: (Boolean) -> Unit,
    private val onContactSupportClicked: () -> Unit
) {
    private val context = binding.root.context

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitSummaryFeature.State>()

    init {
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Loading>(binding.courseBenefitSummaryLoading)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Empty>(binding.courseBenefitSummaryEmpty, binding.courseBenefitOperationDisclaimer)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Content>(binding.courseBenefitSummaryInformation, binding.courseBenefitOperationDisclaimer)

        binding.courseBenefitExperimentDisclaimer.text = buildSpannedString {
            bold { append(context.getString(R.string.course_benefits_contact_support_part_1)) }
            append(context.getString(R.string.course_benefits_contact_support_part_2))

            color(ContextCompat.getColor(context, R.color.color_overlay_violet)) {
                append(context.getString(R.string.course_benefits_contact_support_part_3))
            }
            append(".")
        }

        binding.courseBenefitExperimentDisclaimer.setOnClickListener { onContactSupportClicked() }

        binding.courseBenefitSummaryInformation.setOnClickListener {
            binding.courseBenefitSummaryArrow.changeState()
            val isExpanded = binding.courseBenefitSummaryArrow.isExpanded()
            onCourseSummaryClicked(isExpanded)
            if (isExpanded) {
                binding.courseBenefitSummaryInformationExpansion.expand()
            } else {
                binding.courseBenefitSummaryInformationExpansion.collapse()
            }
        }
    }

    fun render(state: CourseBenefitSummaryFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseBenefitSummaryFeature.State.Content) {
            val currency = Currency.getInstance(state.courseBenefitSummary.currencyCode)
            val decimalFormat = DecimalFormat().apply { setCurrency(currency) }
            decimalFormat.minimumFractionDigits = 2

            val currentMonthDate = DateTimeHelper.getPrintableDate(
                state.courseBenefitSummary.currentDate,
                DateTimeHelper.DISPLAY_MONTH_YEAR_NOMINAL_PATTERN,
                TimeZone.getDefault()
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            val totalDate = DateTimeHelper.getPrintableDate(
                state.courseBenefitSummary.beginPaymentDate,
                DateTimeHelper.DISPLAY_MONTH_YEAR_GENITIVE_PATTERN,
                TimeZone.getDefault()
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            binding.courseBenefitSummaryEarningsCurrentMonthText.text = context.getString(R.string.course_benefits_earning_current_month, currentMonthDate)
            binding.courseBenefitSummaryEarningsCurrentMonthValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.monthUserIncome.toDoubleOrNull() ?: 0.0))

            binding.courseBenefitSummaryTurnoverCurrentMonthText.text = context.getString(R.string.course_benefits_turnover_current_month, currentMonthDate)
            binding.courseBenefitSummaryTurnoverCurrentMonthValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.monthTurnover.toDoubleOrNull() ?: 0.0))

            binding.courseBenefitSummaryEarningsTotalText.text = context.getString(R.string.course_benefits_earnings_total, totalDate)
            binding.courseBenefitSummaryEarningsTotalValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.totalUserIncome.toDoubleOrNull() ?: 0.0))

            binding.courseBenefitSummaryTurnoverTotalText.text = context.getString(R.string.course_beneifts_turnover_total, totalDate)
            binding.courseBenefitSummaryTurnoverTotalValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.totalTurnover.toDoubleOrNull() ?: 0.0))
        }
    }
}
