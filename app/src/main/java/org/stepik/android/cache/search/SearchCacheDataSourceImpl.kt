package org.stepik.android.cache.search

import io.reactivex.Completable
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.search.source.SearchCacheDataSource
import javax.inject.Inject

class SearchCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : SearchCacheDataSource {
    override fun saveSearchQuery(query: String): Completable =
        Completable.fromCallable { databaseFacade.addSearchQuery(SearchQuery(query)) }
}