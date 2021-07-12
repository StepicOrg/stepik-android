package org.stepik.android.presentation.course_benefits.reducer

import org.stepik.android.presentation.course_benefits.CourseBenefitSummaryFeature
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature.State
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature.Message
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature.Action
import org.stepik.android.presentation.course_benefits.CourseBenefitsPurchasesAndRefundsFeature
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitsReducer
@Inject
constructor(
    private val courseBenefitSummaryReducer: CourseBenefitSummaryReducer,
    private val courseBenefitsPurchasesAndRefundsReducer: CourseBenefitsPurchasesAndRefundsReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state.courseBenefitState is CourseBenefitsFeature.CourseBenefitState.Idle ||
                    state.courseBenefitState is CourseBenefitsFeature.CourseBenefitState.Error && message.forceUpdate) {
                        State(
                            courseBenefitState = CourseBenefitsFeature.CourseBenefitState.Loading,
                            courseBenefitSummaryState = CourseBenefitSummaryFeature.State.Loading,
                            courseBenefitsPurchasesAndRefundsState = CourseBenefitsPurchasesAndRefundsFeature.State.Loading
                        ) to setOf(
                            Action.CourseBenefitSummaryAction(CourseBenefitSummaryFeature.Action.FetchCourseBenefitSummary(message.courseId)),
                            Action.CourseBenefitsPurchasesAndRefundsAction(CourseBenefitsPurchasesAndRefundsFeature.Action.FetchCourseBenefits(message.courseId))
                        )
                } else {
                    null
                }
            }
            is Message.CourseBenefitSummaryMessage -> {
                val (courseBenefitSummaryState, courseBenefitSummaryActions) = courseBenefitSummaryReducer.reduce(state.courseBenefitSummaryState, message.message)
                if (courseBenefitSummaryState is CourseBenefitSummaryFeature.State.Error) {
                    State(
                        courseBenefitState = CourseBenefitsFeature.CourseBenefitState.Error,
                        courseBenefitSummaryState = CourseBenefitSummaryFeature.State.Loading,
                        courseBenefitsPurchasesAndRefundsState = CourseBenefitsPurchasesAndRefundsFeature.State.Loading
                    ) to emptySet()
                } else {
                    State(
                        courseBenefitState = CourseBenefitsFeature.CourseBenefitState.Content,
                        courseBenefitSummaryState = courseBenefitSummaryState,
                        courseBenefitsPurchasesAndRefundsState = state.courseBenefitsPurchasesAndRefundsState
                    ) to courseBenefitSummaryActions.map(Action::CourseBenefitSummaryAction).toSet()
                }
            }
            is Message.CourseBenefitsPurchasesAndRefundsMessage -> {
                val (courseBenefitsPurchasesAndRefundsState, courseBenefitsPurchasesAndRefundsActions) =
                    courseBenefitsPurchasesAndRefundsReducer.reduce(state.courseBenefitsPurchasesAndRefundsState, message.message)
                state.copy(courseBenefitsPurchasesAndRefundsState = courseBenefitsPurchasesAndRefundsState) to
                        courseBenefitsPurchasesAndRefundsActions.map(Action::CourseBenefitsPurchasesAndRefundsAction).toSet()
            }
        } ?: state to emptySet()
}