package org.stepik.android.domain.search_result.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import ru.nobird.android.core.model.mapOfNotNull
import java.io.Serializable

@Parcelize
data class SearchResultQuery(
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("tag_id")
    val tagId: Int? = null,
    @SerializedName("query")
    val query: String? = null,
    @SerializedName("filter_query")
    val filterQuery: CourseListFilterQuery? = null
) : Parcelable, Serializable {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"
        private const val LANG = "language"
        private const val IS_PAID = "is_paid"
        private const val WITH_CERTIFICATE = "with_certificate"
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            PAGE to page,
            TAG to tagId,
            QUERY to query,
            LANG to filterQuery?.language,
            IS_PAID to filterQuery?.isPaid,
            WITH_CERTIFICATE to filterQuery?.withCertificate
        )
}