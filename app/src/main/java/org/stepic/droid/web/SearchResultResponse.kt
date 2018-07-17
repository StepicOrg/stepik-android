package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.SearchResult
import org.stepik.android.model.Meta

class SearchResultResponse(
        meta: Meta,
        @SerializedName("search-results")
        val searchResultList: List<SearchResult>
) : MetaResponseBase(meta)