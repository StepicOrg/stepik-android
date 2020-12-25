package org.stepik.android.domain.search_result.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import ru.nobird.android.core.model.mapOfNotNull
import java.io.Serializable

@Parcelize
data class SearchResultQuery(
    val page: Int? = null,
    val tagId: Int? = null,
    val query: String? = null,
    val filterQuery: CourseListFilterQuery? = null,

    val readinessLowerBound: Float = 0.7f,
    val hasLogo: Boolean = true,
    val isIdeaCompatible: Boolean = false
) : Parcelable, Serializable {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"

        private const val READINESS_LOWER_BOUND = "readiness__gte"
        private const val HAS_LOGO = "has_logo"
        private const val IS_IDEA_COMPATIBLE = "is_idea_compatible"
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            PAGE to page,
            TAG to tagId,
            QUERY to query,

            READINESS_LOWER_BOUND to readinessLowerBound,
            HAS_LOGO to hasLogo,
            IS_IDEA_COMPATIBLE to isIdeaCompatible
        ) + (filterQuery?.toMap() ?: emptyMap())
}