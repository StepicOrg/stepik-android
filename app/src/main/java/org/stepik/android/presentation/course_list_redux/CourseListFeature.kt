package org.stepik.android.presentation.course_list_redux

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.course_list.model.CourseListItem

interface CourseListFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object NetworkError : State()

        data class Content(
            val courseListDataItems: PagedList<CourseListItem.Data>,
            val courseListItems: List<CourseListItem>
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val id: Long, val fullCourseList: CatalogBlockContent.FullCourseList, val forceUpdate: Boolean = false) : Message()
        data class FetchCourseListSuccess(
            val id: Long,
            val courseListDataItems: PagedList<CourseListItem.Data>,
            val courseListItems: List<CourseListItem>
        ) : Message()
        data class FetchCourseListError(val id: Long) : Message()
    }

    sealed class Action {
        data class FetchCourseList(val id: Long, val fullCourseList: CatalogBlockContent.FullCourseList) : Action()
        sealed class ViewAction : Action()
    }
}