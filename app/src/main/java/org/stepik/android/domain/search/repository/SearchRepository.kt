package org.stepik.android.domain.search.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery

interface SearchRepository {
    fun saveSearchQuery(query: String): Completable
    fun getSearchQueries(query: String): Single<List<SearchQuery>>
}