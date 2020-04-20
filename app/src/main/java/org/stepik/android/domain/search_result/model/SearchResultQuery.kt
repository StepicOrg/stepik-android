package org.stepik.android.domain.search_result.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultQuery(
    val page: Int? = null,
    val tagId: Int? = null,
    val query: String? = null,
    val lang: String? = null
) : Parcelable