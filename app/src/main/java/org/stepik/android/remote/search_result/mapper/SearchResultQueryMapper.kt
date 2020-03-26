package org.stepik.android.remote.search_result.mapper

import org.stepic.droid.util.putNullable
import org.stepik.android.domain.search_result.model.SearchResultQuery
import javax.inject.Inject

class SearchResultQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"
        private const val LANG = "language"
    }

    fun mapToQueryMap(searchResultQuery: SearchResultQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, searchResultQuery.page?.toString())
        mutableMap.putNullable(TAG, searchResultQuery.tag?.toString())
        mutableMap.putNullable(QUERY, searchResultQuery.query)
        mutableMap.putNullable(LANG, searchResultQuery.lang)

        return mutableMap
    }
}