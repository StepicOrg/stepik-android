package org.stepik.android.remote.search.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.SearchQuery

class QueriesResponse(
    @SerializedName("queries")
    val queries: List<SearchQuery>
)