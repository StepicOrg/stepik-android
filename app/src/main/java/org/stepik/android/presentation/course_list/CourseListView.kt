package org.stepik.android.presentation.course_list

import org.stepik.android.domain.course_list.model.CourseListItem

interface CourseListView {
    sealed class State {
        object Idle : State()
        object Empty : State()
        object Error : State()

        data class Content(val courses: List<CourseListItem>) : State()
    }

    fun setState(state: State)
}
/*
interface StateContainer<T> : ReadWriteProperty<Any, T> {
    var state: T

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        state

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        state = value
    }
}

class DefaultStateContainer<T>(
    initialState: T
): StateContainer<T> {
    override var state: T = initialState
}
*/