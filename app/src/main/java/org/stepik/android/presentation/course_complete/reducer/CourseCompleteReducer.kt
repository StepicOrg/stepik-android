package org.stepik.android.presentation.course_complete.reducer

import org.stepik.android.presentation.course_complete.CourseCompleteFeature.State
import org.stepik.android.presentation.course_complete.CourseCompleteFeature.Message
import org.stepik.android.presentation.course_complete.CourseCompleteFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseCompleteReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> {
        TODO("Not yet implemented")
    }
}