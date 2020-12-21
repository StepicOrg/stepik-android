package org.stepik.android.presentation.course_list.mapper

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.core.model.safeCast
import javax.inject.Inject

class CourseListCollectionStateMapper
@Inject
constructor() {
    fun isNeedLoadNextPage(state: CourseListCollectionView.State): Boolean {
        state as CourseListCollectionView.State.Data

        val lastItem = state.courseListViewState
            .safeCast<CourseListView.State.Content>()
            ?.courseListItems
            ?.lastOrNull()

        return state.sourceType == DataSourceType.CACHE && lastItem is CourseListItem.PlaceHolder
    }
}