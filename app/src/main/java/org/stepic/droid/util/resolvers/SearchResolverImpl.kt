package org.stepic.droid.util.resolvers

import org.stepik.android.model.structure.SearchResult
import javax.inject.Inject

class SearchResolverImpl @Inject constructor() : SearchResolver {

    override fun getCourseIdsFromSearchResults(searchResultList: List<SearchResult>?): LongArray {
        val result = searchResultList
                ?.filter { it.course > 0L }
                ?.map { it.course }
                ?.toLongArray() ?: kotlin.LongArray(0)

        return result
    }
}
