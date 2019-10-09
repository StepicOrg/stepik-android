package org.stepic.droid.util.resolvers

import org.stepik.android.model.SearchResult
import javax.inject.Inject

class SearchResolverImpl @Inject constructor() : SearchResolver {
    override fun getCourseIdsFromSearchResults(searchResultList: List<SearchResult>?): LongArray =
        searchResultList
            ?.filter { it.course > 0L }
            ?.map { it.course }
            ?.toLongArray()
            ?: longArrayOf()
}
