package org.stepik.android.domain.course_collection.model

import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.CourseCollection

data class CourseCollectionResult(
    val courseCollection: CourseCollection,
    val courseListDataItems: PagedList<CourseListItem.Data>,
    val courseListItems: List<CourseListItem>,
    val sourceType: DataSourceType
)
