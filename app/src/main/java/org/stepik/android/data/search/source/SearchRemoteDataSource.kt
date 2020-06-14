package org.stepik.android.data.search.source

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery

interface SearchRemoteDataSource {
    fun getSearchQueries(query: String): Single<List<SearchQuery>>
}