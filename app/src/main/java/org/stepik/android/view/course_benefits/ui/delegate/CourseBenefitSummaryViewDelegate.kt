package org.stepik.android.view.course_benefits.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.view_course_benefit_summary.view.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.presentation.course_benefits.CourseBenefitSummaryFeature
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import java.util.TimeZone
import java.util.Locale

class CourseBenefitSummaryViewDelegate(
    containerView: View,
    private val displayPriceMapper: DisplayPriceMapper
) {
    private val context = containerView.context

    private val courseBenefitsSummaryLoading = containerView.courseBenefitSummaryLoading
    private val courseBenefitSummaryEmpty = containerView.courseBenefitSummaryEmpty
    private val courseBenefitSummaryContainer = containerView.courseBenefitSummaryInformation
    private val courseBenefitOperationDisclaimer = containerView.courseBenefitOperationDisclaimer
    private val courseBenefitCurrentEarningsTitle = containerView.courseBenefitSummaryEarningsCurrentMonthText
    private val courseBenefitCurrentEarningsValue = containerView.courseBenefitSummaryEarningsCurrentMonthValue

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitSummaryFeature.State>()

    init {
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Loading>(courseBenefitsSummaryLoading)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Empty>(courseBenefitSummaryEmpty, courseBenefitOperationDisclaimer)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Content>(courseBenefitSummaryContainer, courseBenefitOperationDisclaimer)
    }

    fun render(state: CourseBenefitSummaryFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseBenefitSummaryFeature.State.Content) {
            courseBenefitCurrentEarningsTitle.text =
                context.getString(
                    R.string.course_benefits_earning_current_month,
                    DateTimeHelper.getPrintableDate(
                        state.courseBenefitSummary.currentDate,
                        DateTimeHelper.DISPLAY_MONTH_YEAR_NOMINAL_PATTERN,
                        TimeZone.getDefault()
                    ).capitalize(Locale.ROOT)
            )
            courseBenefitCurrentEarningsValue.text = displayPriceMapper.mapToDisplayPrice(state.courseBenefitSummary.currencyCode, state.courseBenefitSummary.monthIncome)
        }
    }
}