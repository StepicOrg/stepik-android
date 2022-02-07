package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.State
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.Message
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature.Action
import ru.nobird.app.core.model.concatWithPagedList
import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitsMonthlyReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseBenefitsByMonthsSuccess -> {
                when (state) {
                    is State.Loading -> {
                        val courseBenefitsMonthlyState =
                            if (message.courseBenefitByMonthListDataItems.isEmpty()) {
                                State.Empty
                            } else {
                                State.Content(message.courseBenefitByMonthListDataItems, message.courseBenefitByMonthListDataItems)
                            }
                        courseBenefitsMonthlyState to emptySet()
                    }
                    is State.Content -> {
                        val resultingList = state.courseBenefitByMonthListDataItems.concatWithPagedList(message.courseBenefitByMonthListDataItems)
                        state.copy(courseBenefitByMonthListDataItems = resultingList, courseBenefitByMonthListItems = resultingList) to emptySet()
                    }
                    else ->
                        null
                }
            }
            is Message.FetchCourseBenefitsByMonthsFailure -> {
                when (state) {
                    is State.Loading ->
                        State.Error to emptySet()

                    is State.Content ->
                        state to emptySet()

                    else ->
                        null
                }
            }
            is Message.FetchCourseBenefitsByMonthNext -> {
                if (state is State.Content) {
                    if (state.courseBenefitByMonthListDataItems.hasNext && state.courseBenefitByMonthListItems.last() !is CourseBenefitByMonthListItem.Placeholder) {
                        state.copy(courseBenefitByMonthListItems = state.courseBenefitByMonthListItems + CourseBenefitByMonthListItem.Placeholder) to
                                setOf(Action.FetchCourseBenefitsByMonths(message.courseId, state.courseBenefitByMonthListDataItems.page + 1))
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            is Message.TryAgain -> {
                if (state is State.Error) {
                    State.Loading to setOf(Action.FetchCourseBenefitsByMonths(message.courseId))
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}