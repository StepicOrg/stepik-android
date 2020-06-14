package org.stepik.android.domain.search.repository

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery

interface SearchRepository {
    fun getSearchQueries(query: String): Single<List<SearchQuery>>
}