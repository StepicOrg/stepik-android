package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.State
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.Message
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitsMonthlyReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseBenefitsByMonthsSuccess -> {
                if (state is State.Loading) {
                    val courseBenefitsMonthlyState =
                        if (message.courseBenefitByMonthListItems.isEmpty()) {
                            State.Empty
                        } else {
                            State.Content(message.courseBenefitByMonthListItems)
                        }
                    courseBenefitsMonthlyState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseBenefitsByMonthsFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}