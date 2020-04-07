package org.stepik.android.presentation.course_list

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListView : CourseContinueView {
    sealed class State {
        object Idle : State()
        class Loading(val collectionData: CollectionData? = null) : State()
        object Empty : State()
        object NetworkError : State()

        data class Content(
            val collectionData: CollectionData? = null,
            val courseListDataItems: PagedList<CourseListItem.Data>,
            val courseListItems: List<CourseListItem>
        ) : State()

        data class CollectionData(val title: String, val description: String)
    }

    fun setState(state: State)
    fun showNetworkError()
}