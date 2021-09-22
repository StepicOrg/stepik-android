package org.stepik.android.domain.search.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepik.android.domain.base.DataSourceType

interface SearchRepository {
    fun saveSearchQuery(courseId: Long = -1, query: String): Completable
    fun getSearchQueries(courseId: Long = -1L, query: String, sourceType: DataSourceType): Single<List<SearchQuery>>
}