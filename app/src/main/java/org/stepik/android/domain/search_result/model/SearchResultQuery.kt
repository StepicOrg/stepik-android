package org.stepik.android.domain.search_result.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import java.io.Serializable

@Parcelize
data class SearchResultQuery(
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("tag_id")
    val tagId: Int? = null,
    @SerializedName("query")
    val query: String? = null,
    val filterQuery: CourseListFilterQuery? = null
) : Parcelable, Serializable