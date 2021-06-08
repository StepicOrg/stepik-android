package org.stepik.android.presentation.course_complete.reducer

import org.stepik.android.presentation.course_complete.CourseCompleteFeature.State
import org.stepik.android.presentation.course_complete.CourseCompleteFeature.Message
import org.stepik.android.presentation.course_complete.CourseCompleteFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseCompleteReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.Init ->
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchCourseCompleteInfo(message.course))
                } else {
                    null
                }
            is Message.FetchCourseCompleteInfoSuccess ->
                if (state is State.Loading) {
                    State.Content(message.courseCompleteInfo) to emptySet()
                } else {
                    null
                }
            is Message.FetchCourseCompleteError ->
                if (state is State.Loading) {
                    State.NetworkError to setOf(Action.ViewAction.ShowNetworkError)
                } else {
                    null
                }
        } ?: state to emptySet()
}