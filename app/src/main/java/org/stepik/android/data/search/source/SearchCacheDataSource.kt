package org.stepik.android.data.search.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery

interface SearchCacheDataSource {
    fun saveSearchQuery(searchQuery: SearchQuery): Completable
    fun getSearchQueries(courseId: Long, query: String): Single<List<SearchQuery>>
}