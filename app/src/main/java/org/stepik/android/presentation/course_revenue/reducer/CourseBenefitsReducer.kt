package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.State
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.Message
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseBenefitsSuccess -> {
                if (state is State.Loading) {
                    val courseBenefitsPurchasesAndRefundsState =
                        if (message.courseBenefitListItems.isEmpty()) {
                            State.Empty
                        } else {
                            State.Content(message.courseBenefitListItems)
                        }
                    courseBenefitsPurchasesAndRefundsState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseBenefitsFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}