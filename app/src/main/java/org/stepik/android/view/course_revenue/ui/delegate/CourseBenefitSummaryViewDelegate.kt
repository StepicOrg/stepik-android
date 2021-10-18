package org.stepik.android.view.course_revenue.ui.delegate

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import kotlinx.android.synthetic.main.view_course_benefit_summary.view.*
import org.stepic.droid.R
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
    containerView: View,
    private val revenuePriceMapper: RevenuePriceMapper,
    private val onCourseSummaryClicked: (Boolean) -> Unit,
    private val onContactSupportClicked: () -> Unit
) {
    private val context = containerView.context

    private val courseBenefitsSummaryLoading = containerView.courseBenefitSummaryLoading
    private val courseBenefitSummaryEmpty = containerView.courseBenefitSummaryEmpty

    private val courseBenefitSummaryContainer = containerView.courseBenefitSummaryInformation
    private val courseBenefitSummaryInformationExpansion = containerView.courseBenefitSummaryInformationExpansion

    private val courseBenefitSummaryArrow = containerView.courseBenefitSummaryArrow
    private val courseBenefitExperimentDisclaimer = containerView.courseBenefitExperimentDisclaimer
    private val courseBenefitOperationDisclaimer = containerView.courseBenefitOperationDisclaimer

    private val courseBenefitCurrentEarningsTitle = containerView.courseBenefitSummaryEarningsCurrentMonthText
    private val courseBenefitCurrentEarningsValue = containerView.courseBenefitSummaryEarningsCurrentMonthValue

    private val courseBenefitCurrentTurnoverTitle = containerView.courseBenefitSummaryTurnoverCurrentMonthText
    private val courseBenefitCurrentTurnoverValue = containerView.courseBenefitSummaryTurnoverCurrentMonthValue

    private val courseBenefitTotalEarningsTitle = containerView.courseBenefitSummaryEarningsTotalText
    private val courseBenefitTotalEarningsValue = containerView.courseBenefitSummaryEarningsTotalValue

    private val courseBenefitTotalTurnoverTitle = containerView.courseBenefitSummaryTurnoverTotalText
    private val courseBenefitTotalTurnoverValue = containerView.courseBenefitSummaryTurnoverTotalValue

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitSummaryFeature.State>()

    init {
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Loading>(courseBenefitsSummaryLoading)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Empty>(courseBenefitSummaryEmpty, courseBenefitOperationDisclaimer)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Content>(courseBenefitSummaryContainer, courseBenefitOperationDisclaimer)

        courseBenefitExperimentDisclaimer.text = buildSpannedString {
            bold { append(context.getString(R.string.course_benefits_contact_support_part_1)) }
            append(context.getString(R.string.course_benefits_contact_support_part_2))

            color(ContextCompat.getColor(context, R.color.color_overlay_violet)) {
                append(context.getString(R.string.course_benefits_contact_support_part_3))
            }
            append(".")
        }

        courseBenefitExperimentDisclaimer.setOnClickListener { onContactSupportClicked() }

        courseBenefitSummaryContainer.setOnClickListener {
            courseBenefitSummaryArrow.changeState()
            val isExpanded = courseBenefitSummaryArrow.isExpanded()
            onCourseSummaryClicked(isExpanded)
            if (isExpanded) {
                courseBenefitSummaryInformationExpansion.expand()
            } else {
                courseBenefitSummaryInformationExpansion.collapse()
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
            ).capitalize(Locale.ROOT)

            val totalDate = DateTimeHelper.getPrintableDate(
                state.courseBenefitSummary.beginPaymentDate,
                DateTimeHelper.DISPLAY_MONTH_YEAR_GENITIVE_PATTERN,
                TimeZone.getDefault()
            ).capitalize(Locale.ROOT)

            courseBenefitCurrentEarningsTitle.text = context.getString(R.string.course_benefits_earning_current_month, currentMonthDate)
            courseBenefitCurrentEarningsValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.monthUserIncome.toDoubleOrNull() ?: 0.0))

            courseBenefitCurrentTurnoverTitle.text = context.getString(R.string.course_benefits_turnover_current_month, currentMonthDate)
            courseBenefitCurrentTurnoverValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.monthTurnover.toDoubleOrNull() ?: 0.0))

            courseBenefitTotalEarningsTitle.text = context.getString(R.string.course_benefits_earnings_total, totalDate)
            courseBenefitTotalEarningsValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.totalUserIncome.toDoubleOrNull() ?: 0.0))

            courseBenefitTotalTurnoverTitle.text = context.getString(R.string.course_beneifts_turnover_total, totalDate)
            courseBenefitTotalTurnoverValue.text = revenuePriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, decimalFormat.format(state.courseBenefitSummary.totalTurnover.toDoubleOrNull() ?: 0.0))
        }
    }
}