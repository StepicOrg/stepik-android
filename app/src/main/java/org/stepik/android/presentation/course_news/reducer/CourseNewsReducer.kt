package org.stepik.android.presentation.course_news.reducer

import org.stepik.android.presentation.course_news.CourseNewsFeature.State
import org.stepik.android.presentation.course_news.CourseNewsFeature.Message
import org.stepik.android.presentation.course_news.CourseNewsFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseNewsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        state to emptySet()
}