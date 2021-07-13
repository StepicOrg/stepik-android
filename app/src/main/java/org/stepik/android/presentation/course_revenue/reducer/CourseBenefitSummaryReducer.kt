package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature.State
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature.Message
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitSummaryReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseBenefitSummarySuccess -> {
                if (state is State.Loading) {
                    val courseBenefitSummaryState =
                        if (message.courseBenefitSummary == CourseBenefitSummary.EMPTY) {
                            State.Empty
                        } else {
                            State.Content(message.courseBenefitSummary)
                        }
                    courseBenefitSummaryState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseBenefitSummaryFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}