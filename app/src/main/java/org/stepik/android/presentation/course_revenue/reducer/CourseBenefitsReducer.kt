package org.stepik.android.presentation.course_revenue.reducer

import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.State
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.Message
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature.Action
import ru.nobird.android.core.model.concatWithPagedList
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseBenefitsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseBenefitsSuccess -> {
                if (state is State.Loading) {
                    val courseBenefitsState =
                        if (message.courseBenefitListDataItems.isEmpty()) {
                            State.Empty
                        } else {
                            State.Content(message.courseBenefitListDataItems, message.courseBenefitListDataItems, message.courseBeneficiary)
                        }
                    courseBenefitsState to emptySet()
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
            is Message.FetchNextPage -> {
                if (state is State.Content) {
                    if (state.courseBenefitListDataItems.hasNext && state.courseBenefitListItems.last() !is CourseBenefitListItem.Placeholder) {
                        state.copy(courseBenefitListItems = state.courseBenefitListItems + CourseBenefitListItem.Placeholder) to
                                setOf(Action.FetchCourseBenefitsNext(message.courseId, state.courseBenefitListDataItems.page + 1))
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            is Message.FetchCourseBenefitsNextSuccess -> {
                if (state is State.Content) {
                    val resultingItems = state.courseBenefitListDataItems.concatWithPagedList(message.courseBenefitListDataItems)
                    state.copy(courseBenefitListDataItems = resultingItems, courseBenefitListItems = resultingItems) to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseBenefitsNextFailure -> {
                if (state is State.Content) {
                    state.copy(courseBenefitListItems = state.courseBenefitListDataItems) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}