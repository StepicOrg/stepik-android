package org.stepik.android.data.search.source

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.util.PagedList
import org.stepik.android.model.SearchResult

interface SearchRemoteDataSource {
    fun getSearchResultsCourses(searchQuery: org.stepik.android.domain.course_list.model.SearchQuery): Single<PagedList<SearchResult>>
    fun getSearchQueries(query: String): Single<List<SearchQuery>>
}