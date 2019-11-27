package org.stepik.android.remote.search.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.SearchResult
import org.stepik.android.remote.base.model.MetaResponse

class SearchResultResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("search-results")
    val searchResultList: List<SearchResult>
) : MetaResponse