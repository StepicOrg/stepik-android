package org.stepik.android.data.search.source

import io.reactivex.Completable

interface SearchCacheDataSource {
    fun saveSearchQuery(query: String): Completable
}