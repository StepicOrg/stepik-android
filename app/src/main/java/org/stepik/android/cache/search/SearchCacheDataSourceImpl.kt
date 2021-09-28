package org.stepik.android.cache.search

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.search.source.SearchCacheDataSource
import javax.inject.Inject

class SearchCacheDataSourceImpl
@Inject
constructor(
    private val dbElementsCount: Int,
    private val databaseFacade: DatabaseFacade
) : SearchCacheDataSource {
    override fun saveSearchQuery(searchQuery: SearchQuery): Completable =
        Completable.fromCallable { databaseFacade.addSearchQuery(searchQuery) }

    override fun getSearchQueries(courseId: Long, query: String): Single<List<SearchQuery>> =
        Single.fromCallable {
            databaseFacade.getSearchQueries(courseId, query, dbElementsCount)
        }
}