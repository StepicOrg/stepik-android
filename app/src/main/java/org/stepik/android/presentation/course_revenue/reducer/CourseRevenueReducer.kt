package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature.State
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature.Message
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature.Action
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseRevenueReducer
@Inject
constructor(
    private val courseBenefitSummaryReducer: CourseBenefitSummaryReducer,
    private val courseBenefitsReducer: CourseBenefitsReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state.courseRevenueState is CourseRevenueFeature.CourseRevenueState.Idle ||
                    state.courseRevenueState is CourseRevenueFeature.CourseRevenueState.Error && message.forceUpdate) {
                        State(
                            courseRevenueState = CourseRevenueFeature.CourseRevenueState.Loading,
                            courseBenefitSummaryState = CourseBenefitSummaryFeature.State.Loading,
                            courseBenefitsState = CourseBenefitsFeature.State.Loading
                        ) to setOf(
                            Action.CourseBenefitSummaryAction(CourseBenefitSummaryFeature.Action.FetchCourseBenefitSummary(message.courseId)),
                            Action.CourseBenefitsAction(CourseBenefitsFeature.Action.FetchCourseBenefits(message.courseId))
                        )
                } else {
                    null
                }
            }
            is Message.CourseBenefitSummaryMessage -> {
                val (courseBenefitSummaryState, courseBenefitSummaryActions) = courseBenefitSummaryReducer.reduce(state.courseBenefitSummaryState, message.message)
                if (courseBenefitSummaryState is CourseBenefitSummaryFeature.State.Error) {
                    State(
                        courseRevenueState = CourseRevenueFeature.CourseRevenueState.Error,
                        courseBenefitSummaryState = CourseBenefitSummaryFeature.State.Loading,
                        courseBenefitsState = CourseBenefitsFeature.State.Loading
                    ) to emptySet()
                } else {
                    State(
                        courseRevenueState = CourseRevenueFeature.CourseRevenueState.Content,
                        courseBenefitSummaryState = courseBenefitSummaryState,
                        courseBenefitsState = state.courseBenefitsState
                    ) to courseBenefitSummaryActions.map(Action::CourseBenefitSummaryAction).toSet()
                }
            }
            is Message.CourseBenefitsMessage -> {
                val (courseBenefitsPurchasesAndRefundsState, courseBenefitsPurchasesAndRefundsActions) =
                    courseBenefitsReducer.reduce(state.courseBenefitsState, message.message)
                state.copy(courseBenefitsState = courseBenefitsPurchasesAndRefundsState) to
                        courseBenefitsPurchasesAndRefundsActions.map(Action::CourseBenefitsAction).toSet()
            }
        } ?: state to emptySet()
}