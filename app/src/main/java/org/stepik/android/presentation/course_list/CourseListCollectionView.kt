package org.stepik.android.presentation.course_list

import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListCollectionView : CourseContinueView {
    sealed class State {
        object Idle : State()
        data class Data(val courseCollection: CourseCollection, val courseListViewState: CourseListView.State) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}