package org.stepik.android.presentation.course_list.mapper

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.course_list.CourseListWishView
import ru.nobird.app.core.model.safeCast
import javax.inject.Inject

class CourseListWishStateMapper
@Inject
constructor() {
    fun isNeedLoadNextPage(state: CourseListWishView.State): Boolean {
        state as CourseListWishView.State.Data

        val lastItem = state.courseListViewState
            .safeCast<CourseListView.State.Content>()
            ?.courseListItems
            ?.lastOrNull()

        return state.sourceType == DataSourceType.CACHE && lastItem is CourseListItem.PlaceHolder
    }
}