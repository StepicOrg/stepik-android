package org.stepik.android.data.search.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepik.android.data.search.source.SearchCacheDataSource
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.domain.search.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl
@Inject
constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchCacheDataSource: SearchCacheDataSource
) : SearchRepository {
    override fun saveSearchQuery(query: String): Completable =
        searchCacheDataSource.saveSearchQuery(query)

    override fun getSearchQueries(query: String): Single<List<SearchQuery>> =
        searchRemoteDataSource.getSearchQueries(query)
}