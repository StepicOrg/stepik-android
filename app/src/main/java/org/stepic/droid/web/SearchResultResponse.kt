package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.SearchResult

class SearchResultResponse(
        meta: Meta,
        @SerializedName("search-results")
        val searchResultList: List<SearchResult>
) : MetaResponseBase(meta)