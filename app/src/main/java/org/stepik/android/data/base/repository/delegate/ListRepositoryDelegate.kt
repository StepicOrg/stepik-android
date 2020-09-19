package org.stepik.android.data.base.repository.delegate

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import ru.nobird.android.core.model.Identifiable
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import ru.nobird.android.domain.rx.requireSize

class ListRepositoryDelegate<ID, Item : Identifiable<ID>>(
    private val remoteSource: (List<ID>) -> Single<List<Item>>,
    private val cacheSource: (List<ID>) -> Single<List<Item>>,
    private val saveAction: (List<Item>) -> Completable
) {
    fun get(ids: List<ID>, sourceType: DataSourceType, allowFallback: Boolean = false): Single<List<Item>> {
        val remote = remoteSource(ids)
            .doCompletableOnSuccess(saveAction)

        val cache = cacheSource(ids)

        return when (sourceType) {
            DataSourceType.REMOTE ->
                if (allowFallback) {
                    remote.onErrorResumeNext(cache.requireSize(ids.size))
                } else {
                    remote
                }

            DataSourceType.CACHE ->
                if (allowFallback) {
                    cache.flatMap { cachedItems ->
                        val newIds = (ids.toList() - cachedItems.map { it.id })
                        remoteSource(newIds)
                            .doCompletableOnSuccess(saveAction)
                            .map { remoteItems -> (cachedItems + remoteItems) }
                    }
                } else {
                    cache
                }

            else ->
                throw IllegalArgumentException("Unsupported sourceType = $sourceType")
        }.map { items -> items.sortedBy { ids.indexOf(it.id) } }
    }
}