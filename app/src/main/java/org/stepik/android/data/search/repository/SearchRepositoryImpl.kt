package org.stepik.android.data.search.repository

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.util.PagedList
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.domain.search.repository.SearchRepository
import org.stepik.android.model.SearchResult
import javax.inject.Inject

class SearchRepositoryImpl
@Inject
constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource
) : SearchRepository {
    override fun getSearchResultsCourses(page: Int, rawQuery: String?, lang: String): Single<PagedList<SearchResult>> =
        searchRemoteDataSource.getSearchResultsCourses(page, rawQuery, lang)

    override fun getSearchQueries(query: String): Single<List<SearchQuery>> =
        searchRemoteDataSource.getSearchQueries(query)
}