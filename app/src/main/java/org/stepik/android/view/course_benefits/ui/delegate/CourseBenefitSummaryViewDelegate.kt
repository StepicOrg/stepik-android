package org.stepik.android.view.course_benefits.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.view_course_benefit_summary.view.*
import org.stepik.android.presentation.course_benefits.CourseBenefitSummaryFeature
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class CourseBenefitSummaryViewDelegate(
    containerView: View
) {
    private val courseBenefitSummaryEmpty = containerView.courseBenefitSummaryEmpty
    private val courseBenefitSummaryContainer = containerView.courseBenefitSummaryInformation
    private val courseBenefitOperationDisclaimer = containerView.courseBenefitOperationDisclaimer
    private val courseBenefitCurrentEarningsTitle = containerView.courseBenefitSummaryEarningsCurrentMonthText
    private val courseBenefitCurrentEarningsValue = containerView.courseBenefitSummaryEarningsCurrentMonthValue

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitSummaryFeature.State>()

    init {
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Loading>()
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Empty>(courseBenefitSummaryEmpty, courseBenefitOperationDisclaimer)
        viewStateDelegate.addState<CourseBenefitSummaryFeature.State.Content>(courseBenefitSummaryContainer, courseBenefitOperationDisclaimer)
    }

    fun render(state: CourseBenefitSummaryFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseBenefitSummaryFeature.State.Content) {
            courseBenefitCurrentEarningsTitle.text = state.courseBenefitSummary.currentDate.toString()
            courseBenefitCurrentEarningsValue.text = state.courseBenefitSummary.monthIncome
        }
    }
}