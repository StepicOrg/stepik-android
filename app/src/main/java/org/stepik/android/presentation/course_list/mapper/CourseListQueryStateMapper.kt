package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.core.model.safeCast
import javax.inject.Inject

class CourseListQueryStateMapper
@Inject
constructor() {
    fun mapToCourseListLoadedSuccess(query: CourseListQuery, items: PagedList<CourseListItem.Data>, source: SourceTypeComposition): CourseListQueryView.State.Data =
        CourseListQueryView.State.Data(
            query,
            courseListViewState =
                if (items.isNotEmpty()) {
                    CourseListView.State.Content(
                        courseListDataItems = items,
                        courseListItems = items
                    )
                } else {
                    CourseListView.State.Empty
                },
            source.generalSourceType
        )

    /**
     * Если мы находились в кеше, то при скролле до следующей страницы мы добавили фейковый [CourseListItem.PlaceHolder] в конец
     * Теперь, когда мы скачали страницу из сети мы можем проверить на наличие фейкового итема и скачать вторую страницу
     */
    fun isNeedLoadNextPage(state: CourseListQueryView.State): Boolean {
        state as CourseListQueryView.State.Data

        val lastItem = state.courseListViewState
            .safeCast<CourseListView.State.Content>()
            ?.courseListItems
            ?.lastOrNull()

        return state.sourceType == DataSourceType.CACHE && lastItem is CourseListItem.PlaceHolder
    }
}