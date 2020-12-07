package org.stepik.android.presentation.course_list_redux.reducer

import org.stepik.android.presentation.course_list_redux.CourseListFeature.State
import org.stepik.android.presentation.course_list_redux.CourseListFeature.Message
import org.stepik.android.presentation.course_list_redux.CourseListFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseListReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage ->
                if (state is State.Idle ||
                    state is State.NetworkError && message.forceUpdate
                ) {
                    State.Loading to setOf(Action.FetchCourseList(message.id, message.fullCourseList))
                } else {
                    null
                }

            is Message.FetchCourseListSuccess ->
                if (state is State.Loading) {
                    State.Content(message.courseListDataItems, message.courseListItems) to emptySet()
                } else {
                    null
                }

            is Message.FetchCourseListError ->
                if (state is State.Loading) {
                    State.NetworkError to emptySet()
                } else {
                    null
                }
        } ?: state to emptySet()
}