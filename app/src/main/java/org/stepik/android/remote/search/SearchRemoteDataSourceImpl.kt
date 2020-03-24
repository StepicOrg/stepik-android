package org.stepik.android.remote.search

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.util.PagedList
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.model.SearchResult
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.search.mapper.SearchQueryMapper
import org.stepik.android.remote.search.model.QueriesResponse
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.search.service.SearchService
import javax.inject.Inject

class SearchRemoteDataSourceImpl
@Inject
constructor(
    private val searchService: SearchService,
    private val searchQueryMapper: SearchQueryMapper
) : SearchRemoteDataSource {
    override fun getSearchResultsCourses(searchQuery: org.stepik.android.domain.course_list.model.SearchQuery): Single<PagedList<SearchResult>> =
        searchService
            .getSearchResults(searchQueryMapper.mapToQueryMap(searchQuery))
            .map { it.toPagedList(SearchResultResponse::searchResultList) }

    override fun getSearchQueries(query: String): Single<List<SearchQuery>> =
        searchService
            .getSearchQueries(query)
            .map(QueriesResponse::queries)
}