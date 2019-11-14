package org.stepik.android.remote.search

import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.remote.search.model.QueriesResponse
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.search.service.SearchService
import java.net.URLEncoder
import javax.inject.Inject

class SearchRemoteDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val searchService: SearchService
) : SearchRemoteDataSource {
    override fun getSearchResultsCourses(page: Int, rawQuery: String?): Single<SearchResultResponse> {
        val enumSet = sharedPreferenceHelper.filterForFeatured
        val lang = enumSet.iterator().next().language
        val encodedQuery = URLEncoder.encode(rawQuery)

        return searchService.getSearchResults(page, encodedQuery, lang)
    }

    override fun getSearchQueries(query: String): Single<QueriesResponse> =
        searchService.getSearchQueries(query)
}