package org.stepik.android.domain.search_result.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultQuery(
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("tag_id")
    val tagId: Int? = null,
    @SerializedName("query")
    val query: String? = null,
    @SerializedName("lang")
    val lang: String? = null
) : Parcelable