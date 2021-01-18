package org.stepik.android.domain.search_result.model

import org.stepik.android.domain.filter.model.CourseListFilterQuery
import ru.nobird.android.core.model.mapOfNotNull
import java.io.Serializable

data class SearchResultQuery(
    val page: Int? = null,
    val tagId: Int? = null,
    val query: String? = null,
    val filterQuery: CourseListFilterQuery? = null,
    val remoteQueryParams: Map<String, Any>? = null
) : Serializable {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            PAGE to page,
            TAG to tagId,
            QUERY to query
        ) + (filterQuery?.toMap() ?: emptyMap()) + (remoteQueryParams ?: emptyMap())
}