package org.stepik.android.data.search.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepik.android.data.search.source.SearchCacheDataSource
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.search.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl
@Inject
constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchCacheDataSource: SearchCacheDataSource
) : SearchRepository {
    override fun saveSearchQuery(courseId: Long, query: String): Completable =
        searchCacheDataSource.saveSearchQuery(SearchQuery(courseId = courseId, text = query))

    override fun getSearchQueries(courseId: Long, query: String, sourceType: DataSourceType): Single<List<SearchQuery>> =
        when (sourceType) {
            DataSourceType.CACHE ->
                searchCacheDataSource.getSearchQueries(courseId, query)

            DataSourceType.REMOTE ->
                searchRemoteDataSource.getSearchQueries(query)
        }
}